package com.example.binanceparser.plot;

import com.binance.api.client.FuturesIncomeType;
import com.example.binanceparser.domain.IncomeBalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class IncomeChartBuilder implements ChartBuilder<IncomeBalanceState> {

    @Override
    public JFreeChart buildLineChart(List<IncomeBalanceState> logBalanceStates) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Account balance", "Date", "Balance", createTimeSeries(logBalanceStates)
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
        TimeSeries dataset = createTimeSeries(logBalanceStates, plot);
        dataSeries.addSeries(dataset);
        plot.setDataset(dataSeries);
/*        if(r instanceof XYLineAndShapeRenderer) {
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }*/

        return chart;

    }

    private TimeSeries createTimeSeries(List<IncomeBalanceState> incomeBalanceStates, XYPlot plot) {
        final TimeSeries series = new TimeSeries("USD");
        XYLineAndShapeRenderer renderer;

        for (IncomeBalanceState incomeBalanceState : incomeBalanceStates) {
            if (incomeBalanceState.getIncomeType() == FuturesIncomeType.COMMISSION) continue;
            renderer = new XYLineAndShapeRenderer() {
                @Override
                public Paint getItemPaint(int row, int col) {
                    Paint cpaint = getItemColor(row, col);
                    if (cpaint == null) {
                        cpaint = super.getItemPaint(row, col);
                    }
                    return cpaint;
                }

                public Color getItemColor(int row, int col) {
                    if (incomeBalanceState.getIncomeType() == FuturesIncomeType.TRANSFER) return Color.BLUE;
                    else return Color.RED;
                }

                @Override
                protected void drawFirstPassShape(Graphics2D g2, int pass, int series,
                                                  int item, Shape shape) {
                    g2.setStroke(getItemStroke(series, item));
                    Color c1 = getItemColor(series, item);
                    Color c2 = getItemColor(series, item - 1);
                    GradientPaint linePaint = new GradientPaint(0, 0, c1, 0, 300, c2);
                    g2.setPaint(linePaint);
                    g2.draw(shape);
                }

            };
            series.addOrUpdate(dateTimeToDay(incomeBalanceState.getDateTime()),
                    incomeBalanceState.getAvailableBalance());
        }
        return series;
    }


    private Day dateTimeToDay(LocalDate dateTime) {
        return new Day(dateTime.getDayOfMonth(), dateTime.getMonthValue(),
                dateTime.getYear());
    }
}
