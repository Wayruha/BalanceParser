package com.example.binanceparser.plot;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.time.LocalDate;
import java.util.List;

public class SpotUSDTChartBuilder {

    public JFreeChart buildLineChart(List<BalanceState> balanceStates) {
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();

        dataSeries.addSeries(createTimeSeries(balanceStates));

        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );

    }

    private TimeSeries createTimeSeries(List<BalanceState> balanceStates) {
        final TimeSeries series = new TimeSeries("USD");
        for(BalanceState balanceState: balanceStates) {
            series.addOrUpdate(dateTimeToDay(balanceState.getDateTime()),
                    balanceState.getAssets().stream().findFirst().get().getAvailableBalance());
        }
        return series;
    }


    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }

}
