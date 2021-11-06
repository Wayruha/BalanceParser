package com.example.binanceparser.plot;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class USDChartBuilder implements ChartBuilder{

    Map<String, BigDecimal> currencyRate = new HashMap<>();
    {
        currencyRate.put("BUSD", new BigDecimal(1));
        currencyRate.put("USDT", new BigDecimal("0.5"));
    }

    public JFreeChart buildLineChart(List<BalanceState> balanceStates, List<String> assetToTrack){
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();

        dataSeries.addSeries(createTimeSeries(balanceStates));

        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );
    }

    private TimeSeries createTimeSeries(List<BalanceState> balanceStates) {
        final TimeSeries series = new TimeSeries("USD");
        for(BalanceState balanceState: balanceStates) {
            final BalanceState.Asset asset = balanceState.getAssets().stream().findFirst().get();
            series.addOrUpdate(dateTimeToDay(balanceState.getDateTime()), asset.getAvailableBalance());
        }
        return series;
    }

    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }
}
