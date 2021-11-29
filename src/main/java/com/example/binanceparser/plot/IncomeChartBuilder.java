package com.example.binanceparser.plot;

import com.example.binanceparser.domain.IncomeBalanceState;
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

public class IncomeChartBuilder implements ChartBuilder<IncomeBalanceState> {

    @Override
    public JFreeChart buildLineChart(List<IncomeBalanceState> logBalanceStates) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", createTimeSeries(logBalanceStates)
        );

        XYPlot plot = (XYPlot) chart.getPlot();

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }
        return chart;

    }

    private TimeSeriesCollection createTimeSeries(List<IncomeBalanceState> incomeBalanceStates) {
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
        final TimeSeries series = new TimeSeries("USD");
        //final TimeSeries balanceUpdateSeries = new TimeSeries("Balance Update");
        for (IncomeBalanceState incomeBalanceState : incomeBalanceStates) {
            series.addOrUpdate(dateTimeToDay(incomeBalanceState.getDateTime()),
                    incomeBalanceState.getAvailableBalance());
        }
        dataSeries.addSeries(series);

        return dataSeries;
    }


    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }
}
