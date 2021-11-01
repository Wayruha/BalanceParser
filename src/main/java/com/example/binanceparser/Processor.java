package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public BalanceReport run(Config config) throws IOException {
        final File logsDir = new File(config.getInputFilepath());
        List<AbstractEvent> events = eventSource.readEvents(logsDir, config.startBalanceTrackDate, config.finishBalanceTrackDate).stream()
                .filter(event -> event.getSource().equals(config.getSourceToTrack())).collect(Collectors.toList());
        if(events.size() == 0) throw new RuntimeException("Can't find any relevant events");
        final List<BalanceState> balanceStates = algorithm.processEvents(events);
        List<BalanceState.Asset> assetList = new ArrayList<>();
        for(BalanceState balanceState: balanceStates) {
            assetList.add(balanceState.getAssets().stream().
                    filter(a -> a.getAsset().equals(ASSET_TO_TRACK)).findFirst().get());
        }

        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates, ASSET_TO_TRACK);

        final String filename = config.outputDir + "/" + algorithm.getClass().getSimpleName() + "_" + config.getSourceToTrack() + ".jpg";

        BalanceReport balanceReport = new BalanceReport(config.getStartBalanceTrackDate(), config.getFinishBalanceTrackDate(), findMax(assetList), findMin(assetList),
                saveChartToFile(lineChart, filename), assetList.get(assetList.size() - 1).getAvailableBalance().add(assetList.get(0).getAvailableBalance().negate()
        ));

        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }

    private String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        ChartUtils.saveChartAsJPEG(file, chart, 2000, 370);
        return file.getPath();
    }

    private BigDecimal findMax(List<BalanceState.Asset> assetList) {
        BigDecimal max = new BigDecimal(0);
        for(BalanceState.Asset asset : assetList) max = asset.getAvailableBalance().max(max);
        return max;
    }

    private BigDecimal findMin(List<BalanceState.Asset> assetList) {
        BigDecimal min = new BigDecimal(0);
        for(BalanceState.Asset asset : assetList) min = asset.getAvailableBalance().min(min);
        return min;
    }

    @Data
    public static class Config {
        LocalDateTime startBalanceTrackDate;
        LocalDateTime finishBalanceTrackDate;
        String sourceToTrack;
        String inputFilepath;
        String outputDir;
    }

    @Data
    @AllArgsConstructor
    public static class BalanceReport {
        LocalDateTime startBalanceTrackDate;
        LocalDateTime finishBalanceTrackDate;
        BigDecimal max;
        BigDecimal min;
        String outputPath;
        BigDecimal balanceDifference;

        @Override
        public String toString() {
            return "Result:\nStart track date: " + startBalanceTrackDate + "\nFinish track date: " + finishBalanceTrackDate + "\nBalance difference: " + balanceDifference +
                "\nMax value: " + max + "\nMin value: " + min + "\nOutput path: " + outputPath;
        }
    }
}
