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
	public TestAssetChartBuilder(List<String> assetsToTrack) {
		super(assetsToTrack);
	}

	public TestAssetChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		super(assetsToTrack, config);
	}

	@Override
	public JFreeChart buildLineChart(List<SpotIncomeState> incomeStates) {
		TimeSeriesCollection dataSeries = new TimeSeriesCollection();
		getTimeSeriesForEveryAsset(incomeStates).forEach(dataSeries::addSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", dataSeries);
		if (config.isDrawPoints()) {
			chart.getXYPlot().setRenderer(getRenderer());
		}
		return chart;
	}

	private List<TimeSeries> getTimeSeriesForEveryAsset(List<SpotIncomeState> incomeStates) {
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		if (incomeStates.size() != 0) {
			final List<String> assetsToTrack = listAssetsInvolved(incomeStates.get(incomeStates.size() - 1));
			for (int n = 0; n < assetsToTrack.size(); n++) {
				timeSeriesList.add(createTimeSeries(incomeStates, assetsToTrack.get(n), n));
			}
		}
		return timeSeriesList;
	}

	private TimeSeries createTimeSeries(List<SpotIncomeState> incomeStates, String trackedAsset, int row) {
		final TimeSeries series = new TimeSeries(trackedAsset + " balance (USD)");
		int pointsNumber = 0;
		Second previousSecValue = null;
		for (int n = 0; n < incomeStates.size(); n++) {
			SpotIncomeState incomeState = incomeStates.get(n);
			LocalDateTime currentDateTime = incomeState.getDateTime();
			Second currentSecValue = dateTimeToSecond(currentDateTime);
			BigDecimal unlockedAmount = getUnlockedAmount(trackedAsset, incomeState.getTXs());
			// if withdraw or deposit
			if (incomeState.getTXs().stream().anyMatch(transaction -> isTransfer(trackedAsset, transaction))
					|| (trackedAsset.equals(VIRTUAL_USD) && anyTransfer(incomeState.getTXs()))) {
				withdrawPoints.add(new Point(row, pointsNumber++));
			}
			// if sell contains unlocked asset
			else if (unlockedAmount.compareTo(BigDecimal.ZERO) > 0) {
				// creating point counting only locked part
				intermediatePoints.add(new Point(row, pointsNumber++));
				series.addOrUpdate(
						dateTimeToSecond(
								currentDateTime.minusSeconds(secondsBetween(previousSecValue, currentSecValue))),
						incomeState.calculateVirtualUSDBalance(trackedAsset).subtract(unlockedAmount));
				// creating point counting unlocked part
				specialPoints.add(new Point(row, pointsNumber++));
				series.addOrUpdate(currentSecValue, incomeState.calculateVirtualUSDBalance(trackedAsset));
			}
			// all other situations
			else {
				series.addOrUpdate(currentSecValue, incomeState.calculateVirtualUSDBalance(trackedAsset));
			}
			previousSecValue = dateTimeToSecond(incomeState.getDateTime());
		}
		return series;
	}

	private List<String> listAssetsInvolved(SpotIncomeState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? new ArrayList<>(
						lastIncomeState.getCurrentAssets().stream().map(Asset::getAsset).collect(Collectors.toList()))
				: assetsToTrack;
	}
}