package com.example.binanceparser.plot;

import static com.example.binanceparser.Constants.*;
import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssetChartBuilder extends ChartBuilder<EventBalanceState> {
	public AssetChartBuilder(List<String> assetsToTrack) {
		super(assetsToTrack);
	}

	public AssetChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		super(assetsToTrack, config);
	}

	@Override
	public JFreeChart buildLineChart(List<EventBalanceState> eventBalanceStates) {
		getTimeSeriesForEveryAsset(eventBalanceStates).forEach(dataSeries::addSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", dataSeries);
		if (config.isDrawPoints()) {
			chart.getXYPlot().setRenderer(getRenderer());
		}
		return chart;
	}

	private List<TimeSeries> getTimeSeriesForEveryAsset(List<EventBalanceState> incomeStates) {
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		if (incomeStates.size() != 0) {
			final List<String> assetsToTrack = listAssetsInvolved(incomeStates.get(incomeStates.size() - 1));
			for (int n = 0; n < assetsToTrack.size(); n++) {
				timeSeriesList.add(createTimeSeries(incomeStates, assetsToTrack.get(n), n));
			}
		}
		return timeSeriesList;
	}

	private TimeSeries createTimeSeries(List<EventBalanceState> eventStates, String trackedAsset, int row) {
		final TimeSeries series = new TimeSeries(trackedAsset + " balance (USD)");
		for (int n = 0; n < eventStates.size(); n++) {
			EventBalanceState eventBalanceState = eventStates.get(n);
			Asset asset = eventBalanceState.findAsset(trackedAsset);
			if (eventBalanceState.getTransactions().stream().anyMatch(transaction -> isTransfer(trackedAsset, transaction))
					|| (trackedAsset.equals(VIRTUAL_USD) && anyTransfers(eventBalanceState.getTransactions()))) {
				withdrawPoints.add(new Point(row, n));
				withdrawPoints.add(new Point(0, n));
			}
			series.addOrUpdate(dateTimeToSecond(eventBalanceState.getDateTime()), eventBalanceState.getAssetBalance(trackedAsset));
		}
		return series;
	}

	private List<String> listAssetsInvolved(EventBalanceState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? new ArrayList<>(lastIncomeState.getAssets().stream().map(Asset::getAsset).collect(Collectors.toList()))
				: assetsToTrack;
	}
}