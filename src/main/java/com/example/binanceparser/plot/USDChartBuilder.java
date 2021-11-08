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

public class USDChartBuilder implements ChartBuilder {

    final Map<String, BigDecimal> currencyRate;

    public USDChartBuilder(Map<String, BigDecimal> currencyRate) {
        this.currencyRate = currencyRate;
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
            final BigDecimal usdValue = assetToUSD(asset);
            if(usdValue == null) continue;
            series.addOrUpdate(dateTimeToDay(balanceState.getDateTime()), usdValue);
        }
        return series;
    }

    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }

    public BigDecimal assetToUSD(BalanceState.Asset asset) {
        if(!currencyRate.containsKey(asset.getAsset())) return null;
        return asset.getAvailableBalance().multiply(currencyRate.get(asset.getAsset()));
    }
}
