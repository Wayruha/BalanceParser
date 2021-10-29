package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import lombok.Data;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Processor {
    private static final String ASSET_TO_TRACK = "USDT";
    EventSource eventSource;
    CalculationAlgorithm algorithm;
    ChartBuilder chartBuilder;

    public Processor() {
        this.eventSource = new LogsEventSource();
        this.algorithm = new WalletBalanceCalcAlgorithm(ASSET_TO_TRACK);
        this.chartBuilder = new ChartBuilder();
    }

    public void run(Config config) throws IOException {
        final File logsDir = new File(config.getInputFilepath());
        List<AbstractEvent> events = eventSource.readEvents(logsDir).stream()
                .filter(event -> event.getSource().equals(config.getSourceToTrack())).collect(Collectors.toList());
        if(events.size() == 0) throw new RuntimeException("Can't find any relevant events");
        final List<BalanceState> balanceStates = algorithm.processEvents(events);
        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates, ASSET_TO_TRACK);

        final String filename = config.outputDir + "/" + algorithm.getClass().getSimpleName() + "_" + config.getSourceToTrack() + ".jpg";
        saveChartToFile(lineChart, filename);
        System.out.println("Processor done for config: " + config);
    }

    private void saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        ChartUtils.saveChartAsJPEG(file, chart, 2000, 370);
    }

    @Data
    public static class Config {
        String sourceToTrack;
        String inputFilepath;
        String outputDir;
    }
}
