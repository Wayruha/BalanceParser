package com.example.binanceparser.report;

import com.example.binanceparser.Config;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.plot.AssetChartBuilder;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.plot.USDChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {
    ChartBuilder chartBuilder;

    Map<String, BigDecimal> currencyRate = new HashMap<>();
    {
        currencyRate.put("BUSD", new BigDecimal(1));
        currencyRate.put("USDT", new BigDecimal("0.5"));
    }

    public BalanceReport getBalanceReport(Config config, List<BalanceState> balanceStates) throws IOException {
        //build a plot

        if(config.isConvertToUSD()) this.chartBuilder = new USDChartBuilder();
        else this.chartBuilder = new AssetChartBuilder();

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

    // у 2 випадках повертажться значення в $, а чому в інших - ні?
    public BigDecimal assetToUSD(BalanceState.Asset asset) {
        if(!currencyRate.containsKey(asset.getAsset())) return null;
        return asset.getAvailableBalance().multiply(currencyRate.get(asset.getAsset()));
    }

    private BigDecimal calculateBalanceDelta(List<BalanceState.Asset> assetList) {
        final BigDecimal firstAssetBal = assetList.get(0).getAvailableBalance().negate();
        final BigDecimal lastAssetBalance = assetList.get(assetList.size() - 1).getAvailableBalance();
        return lastAssetBalance.add(firstAssetBal);
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
