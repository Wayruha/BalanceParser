package com.example.binanceparser.plot;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AssetChartBuilder implements ChartBuilder<EventBalanceState> {
    final List<String> assetsToTrack;

    public AssetChartBuilder(List<String> assetsToTrack) {
        this.assetsToTrack = assetsToTrack;
    }

    public JFreeChart buildLineChart(List<EventBalanceState> eventBalanceStates) {
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
        for (String asset : assetsToTrack) {
            dataSeries.addSeries(createTimeSeries(eventBalanceStates, asset));
        }
        return ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", dataSeries
        );

    }

    private TimeSeries createTimeSeries(List<EventBalanceState> eventBalanceStates, String assetToTrack) {
        final TimeSeries series = new TimeSeries(assetToTrack);
        for (EventBalanceState eventBalanceState : eventBalanceStates) {
            if (eventBalanceState.getAssets().stream().filter(a -> a.getAsset().equals(assetToTrack)).findFirst().isEmpty())
                continue;
            final Asset asset = eventBalanceState.getAssets().stream().
                    filter(a -> a.getAsset().equals(assetToTrack)).findFirst().get();
            series.addOrUpdate(dateTimeToDay(eventBalanceState.getDateTime()), asset.getAvailableBalance());
        }
        return series;
    }

    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }
}