package com.example.binanceparser.report;

import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.plot.IncomeChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class IncomeReportGenerator extends AbstractBalanceReportGenerator<IncomeBalanceState, IncomeConfig> {

	private final IncomeChartBuilder incomeChartBuilder = new IncomeChartBuilder();

	public IncomeReportGenerator(IncomeConfig config) {
		super(config);
	}

	public BalanceReport getBalanceReport(List<IncomeBalanceState> balanceStates) throws IOException {
		final JFreeChart lineChart = incomeChartBuilder.buildLineChart(balanceStates);

		final List<BigDecimal> balances = balanceStates.stream().map(IncomeBalanceState::getBalanceState)
				.collect(Collectors.toList());
		final String chartPath = config.getOutputDir() + "/" + config.getSubject().get(0) + ".jpg";
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

		final IncomeBalanceState first = balanceStates.get(0);
		final IncomeBalanceState last = balanceStates.get(balanceStates.size() - 1);
		return BalanceReport.builder().startTrackDate(first.getDateTime()).finishTrackDate(last.getDateTime())
			.balanceAtStart(first.getBalanceState()).balanceAtEnd(last.getBalanceState())
			.min(findMinBalance(balances)).max(findMaxBalance(balances)).outputPath(generatedPlotPath)
			.balanceDifference(calculateBalanceDelta(balances)).build();
	}

	private BigDecimal calculateBalanceDelta(List<BigDecimal> assetList) {
		final BigDecimal firstAssetBal = assetList.get(0).negate();
		final BigDecimal lastAssetBalance = assetList.get(assetList.size() - 1);
		return lastAssetBalance.add(firstAssetBal);
	}

	private static String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
		File file = new File(outputFileName);
		ChartUtils.saveChartAsJPEG(file, chart, 1200, 400);
		return file.getPath();
	}

	private static BigDecimal findMaxBalance(List<BigDecimal> balances) {
		if (balances.stream().findFirst().isEmpty())
			return BigDecimal.valueOf(0);
		BigDecimal max = balances.stream().findFirst().get();
		for (BigDecimal balance : balances)
			max = balance.max(max);
		return max;
	}

	private static BigDecimal findMinBalance(List<BigDecimal> balances) {
		if (balances.stream().findFirst().isEmpty())
			return BigDecimal.valueOf(0);
		BigDecimal min = balances.stream().findFirst().get();
		for (BigDecimal balance : balances)
			min = balance.min(min);
		return min;
	}
}
