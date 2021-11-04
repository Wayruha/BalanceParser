package com.example.binanceparser.plot;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.List;
import java.util.stream.Collectors;

public class ChartBuilder {

    public JFreeChart buildLineChart(List<BalanceState> balanceStates, List<String> assetToTrack){
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();

        for(String asset: assetToTrack){
            List<BalanceState> balanceStateAsset = balanceStates.stream().filter(e -> e.getAssets()
                    .stream().anyMatch(a -> a.getAsset().equals(asset))).collect(Collectors.toList());
            dataSeries.addSeries(createTimeSeries(balanceStateAsset, asset));
        }
        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );

    }

    private TimeSeries createTimeSeries(List<BalanceState> balanceStates, String assetToTrack) {
        final TimeSeries series = new TimeSeries(assetToTrack);
        for(BalanceState balanceState: balanceStates) {
            final BalanceState.Asset asset = balanceState.getAssets().stream().
                    filter(a -> a.getAsset().equals(assetToTrack)).findFirst().get();
            series.addOrUpdate(new Day(balanceState.getDateTime().getDayOfMonth(), balanceState.getDateTime().getMonthValue(),
                    balanceState.getDateTime().getYear()), asset.getAvailableBalance());
        }
        return series;
    }
}