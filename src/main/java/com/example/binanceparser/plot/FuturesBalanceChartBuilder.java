package com.example.binanceparser.plot;

import static com.example.binanceparser.Constants.*;
import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.balance.EventBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.TimeSeries;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuturesBalanceChartBuilder extends ChartBuilder<EventBalanceState> {
	public FuturesBalanceChartBuilder(List<String> assetsToTrack) {
		super(assetsToTrack);
	}

	public FuturesBalanceChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		super(assetsToTrack, config);
	}

	@Override
	public JFreeChart buildLineChart(List<EventBalanceState> eventBalanceStates) {
		getTimeSeriesForEveryAsset(eventBalanceStates).forEach(dataSeries::addSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", dataSeries);
		if (config.isDrawPoints()) {
			chart.getXYPlot().setRenderer(getRenderer());
		}
		final LegendItemSource legendSource = formLegend(usdTransferLegendItem());
		chart.addLegend(new LegendTitle(legendSource));
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
		int numberOfPoints = 0;
		for (int n = 0; n < eventStates.size(); n++) {
			EventBalanceState eventBalanceState = eventStates.get(n);
			if (eventBalanceState.getTXs().stream()
					.anyMatch(transaction -> isTransfer(trackedAsset, transaction))
					|| (trackedAsset.equals(VIRTUAL_USD) && anyTransfer(eventBalanceState.getTXs()))) {
				withdrawPoints.add(new Point(row, numberOfPoints));
				withdrawPoints.add(new Point(0, numberOfPoints));
			}
			if (series.addOrUpdate(dateTimeToSecond(eventBalanceState.getDateTime()),
					eventBalanceState.getAssetBalance(trackedAsset)) == null) {
				numberOfPoints++;
			}
		}
		return series;
	}

	private List<String> listAssetsInvolved(EventBalanceState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? new ArrayList<>(
						lastIncomeState.getAssets().stream().map(Asset::getAsset).collect(Collectors.toList()))
				: assetsToTrack;
	}
}