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

import static com.example.binanceparser.Constants.*;

public class TestAssetChartBuilder extends ChartBuilder<SpotIncomeState> {
	TimeSeriesCollection dataSeries;

	public TestAssetChartBuilder(List<String> assetsToTrack) {
		super(assetsToTrack);
		dataSeries = new TimeSeriesCollection();
	}

	public TestAssetChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		super(assetsToTrack, config);
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
			assetsToTrack.stream().forEach((assetTotrack) -> {
				dataSeries.addSeries(new TimeSeries(assetTotrack + " balance (USD)"));
			});
			for (int n = 0; n < assetsToTrack.size(); n++) {
				fillTimeSeries(incomeStates, assetsToTrack.get(n), n);
			}
		}
	}

	private void fillTimeSeries(List<SpotIncomeState> incomeStates, String trackedAsset, int row) {
		final TimeSeries series = dataSeries.getSeries(trackedAsset+ " balance (USD)");
		int pointsNumber = 0;
		Second previousSecValue = null;
		for (int n = 0; n < incomeStates.size(); n++) {
			SpotIncomeState incomeState = incomeStates.get(n);
			LocalDateTime currentDateTime = incomeState.getDateTime();
			Second currentSecValue = dateTimeToSecond(currentDateTime);
			List<Asset> assetsToProcess = getAssetsToProcess(trackedAsset, incomeState.getTXs());
			// if withdraw or deposit
			if (isWithdrawOrDeposit(trackedAsset, incomeState)) {
				withdrawPoints.add(new Point(row, pointsNumber));
			} else if (assetsToProcess.size() != 0) {
				for (Asset asset : assetsToProcess) {
					BigDecimal wholeAmount = incomeState.calculateVirtualUSDBalance(trackedAsset);
					BigDecimal lockedAmount = wholeAmount.subtract(asset.getBalance());
					BigDecimal coeff = asset.getBalance().divide(wholeAmount, MATH_CONTEXT);
					long secondsBetween = secondsBetween(previousSecValue == null ? currentSecValue : previousSecValue,
							currentSecValue);
					LocalDateTime intermTime = currentDateTime
							.minusSeconds(secondsBetween * coeff.intValue());
					TimeSeries stableCoinSeries = dataSeries.getSeries(asset.getAsset()+ " balance (USD)");
					stableCoinSeries.addOrUpdate(dateTimeToSecond(intermTime), lockedAmount);
					int seriesId = dataSeries.getSeriesIndex(asset.getAsset()+ " balance (USD)");
					int intermediateIndex = stableCoinSeries.getIndex(dateTimeToSecond(intermTime));
					updateSpecialAndWithdrawPoints(seriesId, intermediateIndex);
					intermediatePoints.add(new Point(seriesId, intermediateIndex));
					specialPoints.add(new Point(seriesId, intermediateIndex + 1));
				}
			}

			series.addOrUpdate(currentSecValue, incomeState.calculateVirtualUSDBalance(trackedAsset));
			previousSecValue = dateTimeToSecond(incomeState.getDateTime());
			pointsNumber++;
		}
	}

	private boolean isWithdrawOrDeposit(String trackedAsset, SpotIncomeState incomeState) {
		return incomeState.getTXs().stream().anyMatch(transaction -> isTransfer(trackedAsset, transaction))
				|| (trackedAsset.equals(VIRTUAL_USD) && anyTransfer(incomeState.getTXs()));
	}

	private List<String> listAssetsInvolved(SpotIncomeState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? new ArrayList<>(
						lastIncomeState.getCurrentAssets().stream().map(Asset::getAsset).collect(Collectors.toList()))
				: assetsToTrack;
	}
}