package com.example.binanceparser.report;

import com.example.binanceparser.Config;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.plot.ChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ReportGenerator {
    final ChartBuilder chartBuilder;

    final BigDecimal USDT_COURSE_RATE = new BigDecimal(1);
    final BigDecimal BUSD_COURSE_RATE = new BigDecimal("0.5");

    public ReportGenerator(ChartBuilder chartBuilder) {
        this.chartBuilder = chartBuilder;
    }

    public BalanceReport getBalanceReport(Config config, List<BalanceState> balanceStates) throws IOException {
        //build a plot

        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates, config.getAssetsToTrack());
        final List<BalanceState.Asset> assetList = balanceStates.stream()
                .flatMap(bal -> bal.getAssets().stream())
                .collect(Collectors.toList());

        assetList.forEach(a -> a.setAvailableBalance(assetToUSD(a)));

        final String chartPath =  config.getOutputDir() + "/" + config.getSourceToTrack() + ".jpg";
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);
        final BigDecimal delta = calculateBalanceDelta(assetList);

        final BalanceReport balanceReport = new BalanceReport(config.getStartTrackDate(), config.getFinishTrackDate(),
                findMaxBalance(assetList), findMinBalance(assetList), generatedPlotPath, delta);
        return balanceReport;
    }

    public BigDecimal assetToUSD(BalanceState.Asset asset) {
        if(asset.getAsset().equals("USDT")) return asset.getAvailableBalance().multiply(USDT_COURSE_RATE);
        else if(asset.getAsset().equals("BUSD")) return asset.getAvailableBalance().multiply(BUSD_COURSE_RATE);
        return asset.getAvailableBalance();
    }

    private BigDecimal calculateBalanceDelta(List<BalanceState.Asset> assetList) {
        final BigDecimal firstAssetBal = assetList.get(0).getAvailableBalance().negate();
        final BigDecimal lastAssetBalance = assetList.get(assetList.size() - 1).getAvailableBalance();
        final BigDecimal balanceChange = lastAssetBalance.add(firstAssetBal);
        return balanceChange;
    }

    private static String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        ChartUtils.saveChartAsJPEG(file, chart, 2000, 370);
        return file.getPath();
    }

    private static BigDecimal findMaxBalance(List<BalanceState.Asset> assetList) {
        BigDecimal max = new BigDecimal(0);
        for(BalanceState.Asset asset : assetList) max = asset.getAvailableBalance().max(max);
        return max;
    }

    private static BigDecimal findMinBalance(List<BalanceState.Asset> assetList) {
        BigDecimal min = new BigDecimal(0);
        for(BalanceState.Asset asset : assetList) min = asset.getAvailableBalance().min(min);
        return min;
    }
}
