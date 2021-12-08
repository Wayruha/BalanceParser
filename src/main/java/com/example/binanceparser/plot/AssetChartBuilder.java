package com.example.binanceparser.plot;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
        return ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", dataSeries);

    }
    
    //refactored to build chart more accurately(seconds instead of days)
    private TimeSeries createTimeSeries(List<EventBalanceState> eventBalanceStates, String assetToTrack) {
        final TimeSeries series = new TimeSeries(assetToTrack);
        for (EventBalanceState eventBalanceState : eventBalanceStates) {
            final Asset asset = eventBalanceState.findAsset(assetToTrack);
            if(asset == null) continue;
            series.addOrUpdate(dateTimeToSecond(eventBalanceState.getDateTime()), asset.getAvailableBalance());
        }
        return series;
    }

    private Second dateTimeToSecond(LocalDate dateTime) {
    	return new Second(Date.from(dateTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
//        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
//                dateTime.getYear());
    }
}