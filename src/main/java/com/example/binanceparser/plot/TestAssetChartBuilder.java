package com.example.binanceparser.plot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.SpotIncomeState;

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
		for (int n = 0; n < incomeStates.size(); n++) {
			SpotIncomeState incomeState = incomeStates.get(n);
			if (incomeState.getTransactions().stream()
					.anyMatch(transaction -> isTransfer(trackedAsset, transaction))) {
				withdrawPoints.add(new WithdrawPoint(row, n));
				withdrawPoints.add(new WithdrawPoint(0, n));
			}
			series.addOrUpdate(dateTimeToSecond(incomeState.getDateTime()),
					incomeState.calculateVirtualUSDBalance(trackedAsset));
		}
		return series;
	}

	private List<String> listAssetsInvolved(SpotIncomeState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? new ArrayList<>(lastIncomeState.getCurrentAssets().stream().map(Asset::getAsset).collect(Collectors.toList()))
				: assetsToTrack;
	}
}