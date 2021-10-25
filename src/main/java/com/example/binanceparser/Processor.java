package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Processor {

    EventSource eventSource;
    CalculationAlgorithm algorithm;
    ChartBuilder chartBuilder;

    public Processor() {
        this.eventSource = new LogsEventSource();
        this.algorithm = new CalculationAlgorithmImpl();
        this.chartBuilder = new ChartBuilder();
    }


    public void run(String outputFile) throws IOException {
        String path = "C:\\Users\\yarik\\Desktop\\BinanceParser\\src\\main\\java\\com\\example\\binanceparser\\log";
        final List<AbstractEvent> events = eventSource.readEvents(path);
        final List<BalanceState> balanceStates = algorithm.processEvents(events);
        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

        saveChartToFile(lineChart, outputFile);
    }

    private void saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        ChartUtils.saveChartAsJPEG(file, chart, 2000
                , 370);
    }
}
