package com.example.binanceparser.plot;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.EventBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class USDTChartBuilder implements ChartBuilder {

    final Map<String, BigDecimal> currencyRate;

    public USDTChartBuilder(Map<String, BigDecimal> currencyRate) {
        this.currencyRate = currencyRate;
    }

    public JFreeChart buildLineChart(List<BalanceState> logBalanceStates, List<String> assetToTrack){
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
        List<EventBalanceState> balanceStates = logBalanceStates.stream().map(balanceState -> (EventBalanceState) balanceState).collect(Collectors.toList());
        dataSeries.addSeries(createTimeSeries(balanceStates));

        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );
    }

    private TimeSeries createTimeSeries(List<EventBalanceState> eventBalanceStates) {
        final TimeSeries series = new TimeSeries("USD");
        for(EventBalanceState eventBalanceState : eventBalanceStates) {
            final Asset asset = eventBalanceState.getAssets().stream().findFirst().get();
            final BigDecimal usdValue = assetToUSD(asset);
            if(usdValue == null) continue;
            series.addOrUpdate(dateTimeToDay(eventBalanceState.getDateTime()), usdValue);
        }
        return series;
    }

    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }

    public BigDecimal assetToUSD(Asset asset) {
        if(!currencyRate.containsKey(asset.getAsset())) return null;
        return asset.getAvailableBalance().multiply(currencyRate.get(asset.getAsset()));
    }
}
