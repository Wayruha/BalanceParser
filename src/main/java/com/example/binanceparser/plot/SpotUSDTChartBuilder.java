package com.example.binanceparser.plot;

import com.example.binanceparser.domain.EventBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.time.LocalDate;
import java.util.List;

public class SpotUSDTChartBuilder {

    public JFreeChart buildLineChart(List<EventBalanceState> eventBalanceStates) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", createTimeSeries(eventBalanceStates)
        );

        XYPlot plot = (XYPlot) chart.getPlot();

        XYItemRenderer r = plot.getRenderer();
        if(r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }
        return chart;

    }

    private TimeSeriesCollection createTimeSeries(List<EventBalanceState> eventBalanceStates) {
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
        final TimeSeries series = new TimeSeries("USD");
        //final TimeSeries balanceUpdateSeries = new TimeSeries("Balance Update");
        for(EventBalanceState eventBalanceState : eventBalanceStates) {
                series.addOrUpdate(dateTimeToDay(eventBalanceState.getDateTime()),
                    eventBalanceState.getAssets().stream().findFirst().get().getAvailableBalance());
        }
        dataSeries.addSeries(series);

        return dataSeries;
    }


    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }

}
