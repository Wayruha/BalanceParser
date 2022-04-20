package com.example.binanceparser.plot;

import com.example.binanceparser.domain.balance.IncomeBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FuturesIncomeChartBuilder extends ChartBuilder<IncomeBalanceState> {
    @Override
    public JFreeChart buildLineChart(List<IncomeBalanceState> incomeBalanceStates) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Account profit", "Date", "Balance", null);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer r = plot.getRenderer();
        plot.setDataset(createTimeSeries(incomeBalanceStates, plot));
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }

        return chart;
    }

    private TimeSeriesCollection createTimeSeries(List<IncomeBalanceState> incomeBalanceStates, XYPlot plot) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        final TimeSeries series = new TimeSeries("USD");
        XYLineAndShapeRenderer renderer;

        for (IncomeBalanceState incomeBalanceState : incomeBalanceStates) {
            series.addOrUpdate(dateTimeToDay(incomeBalanceState.getDate()),
                    incomeBalanceState.getAvailableBalance());
        }
        dataset.addSeries(series);
        return dataset;
    }

    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }
}