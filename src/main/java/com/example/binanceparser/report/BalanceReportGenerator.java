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
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.USD;

public class BalanceReportGenerator {
    private ChartBuilder<EventBalanceState> chartBuilder;
    private final BalanceVisualizerConfig config;
    private final Map<String, BigDecimal> currencyRate;

    public BalanceReportGenerator(BalanceVisualizerConfig config, Map<String, BigDecimal> currencyRate) {
        this.config = config;
        this.currencyRate = currencyRate;
        final List<String> assetsToTrack = config.isConvertToUSD() ?
                List.of(USD) : config.getAssetsToTrack();
        this.chartBuilder = new AssetChartBuilder(assetsToTrack);
    }

    public BalanceReport getBalanceReport(List<EventBalanceState> balanceStates) throws IOException {
        // convert all assets to combined $-value
        if (config.isConvertToUSD()) {
            for (EventBalanceState state : balanceStates) {
                final Optional<BigDecimal> optBalance = totalBalance(state.getAssets());
                optBalance.ifPresent(bal -> state.getAssets().add(new Asset(USD, bal)));
            }
        }
        Collections.sort(balanceStates, Comparator.comparing(EventBalanceState::getDateTime));

        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);
        final List<Asset> assetList = balanceStates.stream()
                .flatMap(bal -> bal.getAssets().stream())
                .collect(Collectors.toList());

        final String chartPath = config.getOutputDir() + "/" + config.getSubject().get(0) + ".jpg";
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);
        final BigDecimal delta = calculateBalanceDelta(assetList);

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


    public Optional<BigDecimal> totalBalance(Set<Asset> assets) {
        return assets.stream()
                .map(this::assetToUSD)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add);
    }

    public BigDecimal assetToUSD(Asset asset) {
        if (!currencyRate.containsKey(asset.getAsset())) return null;
        return asset.getAvailableBalance().multiply(currencyRate.get(asset.getAsset()));
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
        if (assetList.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
        BigDecimal max = assetList.stream().findFirst().get().getAvailableBalance();
        for (Asset asset : assetList) max = asset.getAvailableBalance().max(max);
        return max;
    }

    private static BigDecimal findMinBalance(List<Asset> assetList) {
        if (assetList.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
        BigDecimal min = assetList.stream().findFirst().get().getAvailableBalance();
        for (Asset asset : assetList) min = asset.getAvailableBalance().min(min);
        return min;
    }
}
