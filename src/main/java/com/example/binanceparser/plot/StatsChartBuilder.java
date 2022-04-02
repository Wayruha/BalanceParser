package com.example.binanceparser.plot;

import com.example.binanceparser.domain.Stats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.math.BigDecimal;
import java.util.Map;

public class StatsChartBuilder {
    public JFreeChart buildPriceDeviationChart(Stats stats) {
        Map<BigDecimal, Integer> dataset = stats.getPriceDataset();
        Config config = new Config("Price Deviation", "Deviation", "Occur");
        JFreeChart chart = buildChart(dataset, config);
        LegendItem item = new LegendItem("Average price deviation(%): " + stats.getAveragePriceDeviation());
        applyLegend(chart, item);
        return chart;
    }

    public JFreeChart buildDelayChart(Stats stats) {
        Map<BigDecimal, Integer> dataset = stats.getDelayDataset();
        Config config = new Config("Cloning delay", "Delay", "Occur");
        JFreeChart chart = buildChart(dataset, config);
        LegendItem item = new LegendItem("Average delay (ms): " + stats.getAverageDelayDeviation());
        applyLegend(chart, item);
        return chart;
    }

    public JFreeChart buildIncomeDeviationChart(Stats stats) {
        Map<BigDecimal, Integer> dataset = stats.getIncomeDataset();
        Config config = new Config("Income Deviation", "Deviation", "Occur");
        JFreeChart chart = buildChart(dataset, config);
        LegendItem item = new LegendItem("Average income deviation(%): " + stats.getAverageIncomeDeviation());
        applyLegend(chart, item);
        return chart;
    }

    private void applyLegend(JFreeChart chart, LegendItem... items) {
        chart.addLegend(new LegendTitle(() -> {
            LegendItemCollection collection = new LegendItemCollection();
            for (LegendItem item : items) {
                collection.add(item);
            }
            return collection;
        }));
    }

    private JFreeChart buildChart(Map<BigDecimal, Integer> dataset, Config config) {
        XYSeriesCollection data = new XYSeriesCollection();
        XYSeries series = new XYSeries(config.getTitle());
        dataset.keySet().forEach((val) -> {
            Integer occur = dataset.get(val);
            series.addOrUpdate(val, occur);
        });
        data.addSeries(series);
        return ChartFactory.createXYLineChart(config.getTitle(), config.getXAxisLabel(), config.getYAxisLabel(), data);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private final class Config {
        private String title;
        private String xAxisLabel;
        private String yAxisLabel;
    }
}
