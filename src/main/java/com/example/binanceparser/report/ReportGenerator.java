package com.example.binanceparser.report;

import com.example.binanceparser.Config;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.plot.IncomeChartBuilder;
import com.example.binanceparser.plot.SpotUSDTChartBuilder;
import lombok.AllArgsConstructor;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    final IncomeChartBuilder incomeChartBuilder = new IncomeChartBuilder();

    public BalanceReport getBalanceReport(Config config, List<IncomeBalanceState> balanceStates) throws IOException {
/*        if (config.isConvertToUSD()) this.chartBuilder = new SpotAssetChartBuilder();
        else this.chartBuilder = new AssetChartBuilder();*/

        final JFreeChart lineChart = incomeChartBuilder.buildLineChart(balanceStates);
        /*final List<Asset> assetList = balanceStates.stream()
                .flatMap(bal -> bal.getAssets().stream())
                .collect(Collectors.toList());*/

        final String chartPath = config.getOutputDir() + "/" + config.getLogProducer() + ".jpg";
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);
        //final BigDecimal delta = calculateBalanceDelta(assetList);

        final BalanceReport balanceReport = new BalanceReport(config.getStartTrackDate(), config.getFinishTrackDate(),
                null, null, generatedPlotPath, null);
        return balanceReport;
    }

/*    public BigDecimal assetToUSD(Asset asset) {
        if (!currencyRate.containsKey(asset.getAsset())) return null;
        return asset.getAvailableBalance().multiply(currencyRate.get(asset.getAsset()));
    }*/

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
        if(assetList.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
        BigDecimal max = assetList.stream().findFirst().get().getAvailableBalance();
        for (Asset asset : assetList) max = asset.getAvailableBalance().max(max);
        return max;
    }

    private static BigDecimal findMinBalance(List<Asset> assetList) {
        if(assetList.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
        BigDecimal min = assetList.stream().findFirst().get().getAvailableBalance();
        for (Asset asset : assetList) min = asset.getAvailableBalance().min(min);
        return min;
    }
}
