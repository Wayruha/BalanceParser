package com.example.binanceparser.report;

import com.example.binanceparser.Config;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.plot.IncomeChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class IncomeReportGenerator {

    final IncomeChartBuilder incomeChartBuilder = new IncomeChartBuilder();

    public BalanceReport getBalanceReport(Config config, List<IncomeBalanceState> balanceStates) throws IOException {

        final JFreeChart lineChart = incomeChartBuilder.buildLineChart(balanceStates);


        final List<BigDecimal> balances = balanceStates.stream().map(IncomeBalanceState::getAvailableBalance).collect(Collectors.toList());
        final String chartPath = config.getOutputDir() + "/" + "income.jpg";
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

        return new BalanceReport(config.getStartTrackDate(), config.getFinishTrackDate(),
                findMaxBalance(balances), findMinBalance(balances), generatedPlotPath, calculateBalanceDelta(balances));
    }

    private BigDecimal calculateBalanceDelta(List<BigDecimal> assetList) {
        final BigDecimal firstAssetBal = assetList.get(0).negate();
        final BigDecimal lastAssetBalance = assetList.get(assetList.size() - 1);
        return lastAssetBalance.add(firstAssetBal);
    }

    private static String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        ChartUtils.saveChartAsJPEG(file, chart, 2000, 370);
        return file.getPath();
    }

    private static BigDecimal findMaxBalance(List<BigDecimal> balances) {
        if(balances.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
        BigDecimal max = balances.stream().findFirst().get();
        for (BigDecimal balance : balances) max = balance.max(max);
        return max;
    }

    private static BigDecimal findMinBalance(List<BigDecimal> balances) {
        if(balances.stream().findFirst().isEmpty()) return BigDecimal.valueOf(0);
        BigDecimal min = balances.stream().findFirst().get();
        for (BigDecimal balance : balances) min = balance.min(min);
        return min;
    }
}
