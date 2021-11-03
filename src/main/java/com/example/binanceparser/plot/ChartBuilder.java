package com.example.binanceparser.plot;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.util.List;

public class ChartBuilder {

    public JFreeChart buildLineChart(List<BalanceState> balanceStates, String assetToTrack){
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();

/*
        for(BalanceState balanceState: balanceStates) {
            TimeSeries timeSeries = createTimeSeries(lineConfig.getBalanceStateList(), lineConfig.getAssetName());
            dataSeries.addSeries(timeSeries);
        }
*/      dataSeries.addSeries(createTimeSeries(balanceStates, assetToTrack));

        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );

    }

    private TimeSeries createTimeSeries(List<BalanceState> balanceStates, String assetToTrack) {
        final TimeSeries series = new TimeSeries("Date");
        for(BalanceState balanceState: balanceStates) {
            final BalanceState.Asset asset = balanceState.getAssets().stream().
                    filter(a -> a.getAsset().equals(assetToTrack)).findFirst().get();
            series.addOrUpdate(new Day(balanceState.getDateTime().getDayOfMonth(), balanceState.getDateTime().getMonthValue(), balanceState.getDateTime().getYear()), asset.getAvailableBalance());
        }
        return series;
    }
}