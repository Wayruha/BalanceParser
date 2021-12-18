package com.example.binanceparser.plot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.example.binanceparser.domain.SpotIncomeState;

public class TestAssetChartBuilder implements ChartBuilder<SpotIncomeState> {

	final List<String> assetsToTrack;

	public TestAssetChartBuilder(List<String> assetsToTrack) {
		this.assetsToTrack = assetsToTrack;
	}

	@Override
	public JFreeChart buildLineChart(List<SpotIncomeState> logBalanceStates) {
		final TimeSeries series = new TimeSeries("Overall income (USD)");
		for (SpotIncomeState state : logBalanceStates) {
			series.addOrUpdate(dateTimeToSecond(state.getDateTime()), state.getBalanceState());
		}
		return ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", new TimeSeriesCollection(series));
	}
	
	private Second dateTimeToSecond(LocalDateTime dateTime) {
        return new Second(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

}
