package com.example.binanceparser.plot;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.time.LocalDate;
import java.util.List;

public class AssetChartBuilder implements ChartBuilder{

    public JFreeChart buildLineChart(List<BalanceState> balanceStates, List<String> assetsToTrack) {
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();

        for (String asset : assetsToTrack) {
            dataSeries.addSeries(createTimeSeries(balanceStates, asset));
        }
        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );

    }

    private TimeSeries createTimeSeries(List<BalanceState> balanceStates, String assetToTrack) {
        final TimeSeries series = new TimeSeries(assetToTrack);
        for (BalanceState balanceState : balanceStates) {
            if(balanceState.getAssets().stream().filter(a -> a.getAsset().equals(assetToTrack)).findFirst().isEmpty()) continue;
            final BalanceState.Asset asset = balanceState.getAssets().stream().
                    filter(a -> a.getAsset().equals(assetToTrack)).findFirst().get();
            series.addOrUpdate(dateTimeToDay(balanceState.getDateTime()), asset.getAvailableBalance());
        }
        return series;
    }

    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }
}