package com.example.binanceparser.report;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.plot.AssetChartBuilder;
import com.example.binanceparser.plot.ChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.USD;

public class BalanceReportGenerator {
    private ChartBuilder<EventBalanceState> chartBuilder;
    private final BalanceVisualizerConfig config;

    public BalanceReportGenerator(BalanceVisualizerConfig config) {
        this.config = config;
        final List<String> assetsToTrack = config.isConvertToUSD() ?
                List.of(USD) : config.getAssetsToTrack();
        this.chartBuilder = new AssetChartBuilder(assetsToTrack);
    }

    public BalanceReport getBalanceReport(List<EventBalanceState> balanceStates) throws IOException {
        Collections.sort(balanceStates, Comparator.comparing(EventBalanceState::getDateTime));
        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

        final List<Asset> assetList = balanceStates.stream()
                .flatMap(bal -> bal.getAssets().stream())
                .collect(Collectors.toList());
        
        final String chartPath = new StringBuilder()
        		.append(config.getOutputDir())
        		.append("/")
        		.append(config.getSubject().get(0))
        		.append(".jpg")
        		.toString();
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);
        final BigDecimal delta = calculateBalanceDelta(assetList);
        final BigDecimal balanceUpdateDelta = findBalanceUpdateDelta(balanceStates);
        System.out.println(balanceUpdateDelta);
        return BalanceReport.builder()
                .startTrackDate(config.getStartTrackDate())
                .finishTrackDate(config.getFinishTrackDate())
                .balanceAtStart(balanceStates.get(0).getAssetBalance(USD))
                .balanceAtEnd(balanceStates.get(balanceStates.size() - 1).getAssetBalance(USD))
                .min(findMinBalance(assetList))
                .max(findMaxBalance(assetList))
                .outputPath(generatedPlotPath)
                .balanceDifference(delta)
                .build();
    }

    private BigDecimal findBalanceUpdateDelta(List<EventBalanceState> balanceStates) {
        BigDecimal delta = balanceStates.stream().map(EventBalanceState::getBalanceUpdateDelta)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return delta;
    }

    private BigDecimal calculateBalanceDelta(List<Asset> assetList) {
        final BigDecimal firstAssetBal = assetList.get(0).getAvailableBalance().negate();
        final BigDecimal lastAssetBalance = assetList.get(assetList.size() - 1).getAvailableBalance();
        return lastAssetBalance.add(firstAssetBal);
    }

    private static String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        ChartUtils.saveChartAsJPEG(file, chart, 2000, 370);
        return file.getPath();
    }

    private static BigDecimal findMaxBalance(List<Asset> assetList) {
    	BigDecimal max = BigDecimal.valueOf(0);
    	for (Asset asset : assetList) max = asset.getAvailableBalance().max(max);
//        if (assetList.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
//        BigDecimal max = assetList.stream().findFirst().get().getAvailableBalance();
//        for (Asset asset : assetList) max = asset.getAvailableBalance().max(max);
        return max;
    }

    private static BigDecimal findMinBalance(List<Asset> assetList) {
        BigDecimal min = assetList.stream().findFirst().isEmpty()?
        		BigDecimal.valueOf(0)
        		:
        		assetList.stream().findFirst().get().getAvailableBalance();
        for (Asset asset : assetList) min = asset.getAvailableBalance().min(min);
        return min;
    }
}
