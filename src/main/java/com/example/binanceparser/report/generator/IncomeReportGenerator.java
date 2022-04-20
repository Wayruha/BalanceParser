package com.example.binanceparser.report.generator;

import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.domain.balance.IncomeBalanceState;
import com.example.binanceparser.plot.FuturesIncomeChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncomeReportGenerator extends AbstractBalanceReportGenerator<IncomeBalanceState, IncomeConfig> {
	private static final String CHART_EXTENSIONS = ".jpg";
	private static final String CHART_PREFIX = "";
	private static final String CHART_SUFFIX = "_FuturesIncome";

	private final FuturesIncomeChartBuilder futuresIncomeChartBuilder;

	public IncomeReportGenerator(@Qualifier(BeanNames.INCOME_CONFIG) IncomeConfig config, @Qualifier(BeanNames.FUTURES_INCOME_CHART_BUILDER) FuturesIncomeChartBuilder futuresIncomeChartBuilder) {
		super(config);
		this.futuresIncomeChartBuilder = futuresIncomeChartBuilder;
	}

	public BalanceReport getBalanceReport(List<IncomeBalanceState> balanceStates) throws IOException {
		final JFreeChart lineChart = futuresIncomeChartBuilder.buildLineChart(balanceStates);

		final List<BigDecimal> balances = balanceStates.stream().map(IncomeBalanceState::getAvailableBalance)
				.collect(Collectors.toList());
		final String chartPath = config.getOutputDir() + "/" + CHART_PREFIX + config.getSubjects().get(0) + CHART_SUFFIX + CHART_EXTENSIONS;
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

		final IncomeBalanceState first = balanceStates.get(0);
		final IncomeBalanceState last = balanceStates.get(balanceStates.size() - 1);
		return BalanceReport.builder().startTrackDate(first.getDateTime()).finishTrackDate(last.getDateTime())
			.balanceAtStart(first.getAvailableBalance()).balanceAtEnd(last.getAvailableBalance())
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
