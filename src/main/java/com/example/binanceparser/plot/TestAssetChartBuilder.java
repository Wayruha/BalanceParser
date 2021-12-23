package com.example.binanceparser.plot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.Transaction;

public class TestAssetChartBuilder implements ChartBuilder<SpotIncomeState> {

	final List<String> assetsToTrack;

	public TestAssetChartBuilder(List<String> assetsToTrack) {
		this.assetsToTrack = assetsToTrack;
	}

	@Override
	public JFreeChart buildLineChart(List<SpotIncomeState> incomeStates) {
		final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
		dataSeries.addSeries(getOverallIncomeTimeSeries(incomeStates));
		getTimeSeriesForEveryAsset(incomeStates).stream().forEach((timeSeries) -> dataSeries.addSeries(timeSeries));
		return ChartFactory.createTimeSeriesChart("Account income", "Date", "Income", dataSeries);
	}

	private TimeSeries getOverallIncomeTimeSeries(List<SpotIncomeState> incomeStates) {
		final TimeSeries series = new TimeSeries("Overall income (USD)");
		for (SpotIncomeState state : incomeStates) {
			series.addOrUpdate(dateTimeToSecond(state.getDateTime()), state.getBalanceState().doubleValue());
		}
		return series;
	}

	// building income chart for every asset
	private List<TimeSeries> getTimeSeriesForEveryAsset(List<SpotIncomeState> incomeStates) {
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		for (String assetToTrack : assetsToTrack) {
			timeSeriesList.add(createTimeSeries(incomeStates, assetToTrack));
		}
		return timeSeriesList;
	}

	//not final
	private TimeSeries createTimeSeries(List<SpotIncomeState> incomeStates, String assetToTrack) {
		final TimeSeries series = new TimeSeries(assetToTrack + " income (USD)");
		BigDecimal assetIncome = BigDecimal.ZERO;
		for (SpotIncomeState incomeState : incomeStates) {
			Transaction transaction = incomeState.getTransactions().get(0);
			if(transaction.getBaseAsset().equals(assetToTrack)) {
				assetIncome.add(transaction.getIncome());
				series.add(dateTimeToSecond(incomeState.getDateTime()), transaction.getIncome());
			}
		}
		return series;
	}

	private Second dateTimeToSecond(LocalDateTime dateTime) {
		return new Second(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
	}

}
