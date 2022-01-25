package com.example.binanceparser.plot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.SpotIncomeState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.binanceparser.Constants.*;

public class TestAssetChartBuilder extends ChartBuilder<SpotIncomeState> {
	private TimeSeriesCollection dataSeries;
	protected List<PointTime> specialPointTimes;
	protected List<PointTime> intermediatePointTimes;

	public TestAssetChartBuilder(List<String> assetsToTrack) {
		super(assetsToTrack);
		specialPointTimes = new ArrayList<>();
		intermediatePointTimes = new ArrayList<>();
		dataSeries = new TimeSeriesCollection();
	}

	public TestAssetChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		super(assetsToTrack, config);
		specialPointTimes = new ArrayList<>();
		intermediatePoints = new ArrayList<>();
		dataSeries = new TimeSeriesCollection();
	}

	@Override
	public JFreeChart buildLineChart(List<SpotIncomeState> incomeStates) {
		addSeriesForEveryAsset(incomeStates);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", dataSeries);
		if (config.isDrawPoints()) {
			chart.getXYPlot().setRenderer(getRenderer());
		}
		return chart;
	}

	private void addSeriesForEveryAsset(List<SpotIncomeState> incomeStates) {
		if (incomeStates.size() != 0) {
			final List<String> assetsToTrack = listAssetsInvolved(incomeStates.get(incomeStates.size() - 1));
			assetsToTrack.forEach((assetTotrack) -> {
				dataSeries.addSeries(new TimeSeries(assetTotrack + " balance (USD)"));
			});
			for (int n = 0; n < assetsToTrack.size(); n++) {
				fillTimeSeries(incomeStates, assetsToTrack.get(n), n);
			}
			pointTimeToPoint();
		}
	}

	private void fillTimeSeries(List<SpotIncomeState> incomeStates, String trackedAsset, int row) {
		final TimeSeries series = dataSeries.getSeries(trackedAsset + " balance (USD)");
		Second previousSecValue = null;
		for (int n = 0; n < incomeStates.size(); n++) {
			SpotIncomeState incomeState = incomeStates.get(n);
			LocalDateTime currentDateTime = incomeState.getDateTime();
			Second currentSecValue = dateTimeToSecond(currentDateTime);
			List<Asset> assetsToProcess = getAssetsToProcess(trackedAsset, incomeState.getTXs());
			// if withdraw or deposit
			if (isWithdrawOrDeposit(trackedAsset, incomeState)) {
				withdrawPoints.add(new Point(row, n));
			} else if (assetsToProcess.size() != 0) { 
				// не стал выносить в другой метод, 
				// потому что довольно много переменных учтеноб мне кажется читаемость не улучшится
				// да и я поубирал закомменченое и вроде не так много кода, чтобы выносить
				for (Asset asset : assetsToProcess) {
					BigDecimal wholeAmount = incomeState.calculateVirtualUSDBalance(asset.getAsset());
					BigDecimal lockedAmount = wholeAmount.subtract(asset.getBalance());
					BigDecimal coeff = asset.getBalance().divide(wholeAmount, MATH_CONTEXT);
					long secondsBetween = secondsBetween(previousSecValue == null ? currentSecValue : previousSecValue,
							currentSecValue);
					LocalDateTime intermTime = currentDateTime
							.minusSeconds((int) (secondsBetween * coeff.doubleValue()));
					TimeSeries stableCoinSeries = dataSeries.getSeries(asset.getAsset() + " balance (USD)");
					stableCoinSeries.addOrUpdate(dateTimeToSecond(intermTime), lockedAmount);
					intermediatePointTimes.add(new PointTime(asset.getAsset() + " balance (USD)", intermTime));
					specialPointTimes.add(new PointTime(asset.getAsset() + " balance (USD)", currentDateTime));
				}
			}

			series.addOrUpdate(currentSecValue, incomeState.calculateVirtualUSDBalance(trackedAsset));
			previousSecValue = dateTimeToSecond(incomeState.getDateTime());
		}
	}

	private boolean isWithdrawOrDeposit(String trackedAsset, SpotIncomeState incomeState) {
		return incomeState.getTXs().stream().anyMatch(transaction -> isTransfer(trackedAsset, transaction))
				|| (trackedAsset.equals(VIRTUAL_USD) && anyTransfer(incomeState.getTXs()));
	}

	private List<String> listAssetsInvolved(SpotIncomeState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? lastIncomeState.getCurrentAssets().stream().map(Asset::getAsset).collect(Collectors.toList())
				: assetsToTrack;
	}

	private void pointTimeToPoint() {
		specialPointTimes.stream().forEach((pt) -> {
			TimeSeries stableCoinSeries = dataSeries.getSeries(pt.name);
			int row = dataSeries.getSeriesIndex(pt.name);
			int item = stableCoinSeries.getIndex(dateTimeToSecond(pt.getTime()));
			specialPoints.add(new Point(row, item));
		});
		intermediatePointTimes.stream().forEach((pt) -> {
			TimeSeries stableCoinSeries = dataSeries.getSeries(pt.name);
			int row = dataSeries.getSeriesIndex(pt.name);
			int item = stableCoinSeries.getIndex(dateTimeToSecond(pt.getTime()));
			intermediatePoints.add(new Point(row, item));
		});
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	class PointTime {
		private String name;
		private LocalDateTime time;
	}
}