package com.example.binanceparser.report.generator;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.*;
import com.example.binanceparser.domain.balance.EventBalanceState;
import com.example.binanceparser.domain.transaction.Transaction;
import com.example.binanceparser.domain.transaction.TransactionType;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static com.example.binanceparser.Constants.*;
import static java.util.Objects.isNull;

public class FuturesBalanceReportGenerator extends AbstractBalanceReportGenerator<EventBalanceState, BalanceVisualizerConfig> {
	private static EnumSet<TransactionType> tradeTransactionTypes = EnumSet.of(TransactionType.BUY, TransactionType.SELL, TransactionType.CONVERT);
	private static EnumSet<TransactionType> transferTxTypes = EnumSet.of(TransactionType.DEPOSIT, TransactionType.WITHDRAW);
	private static final String DEFAULT_CHART_NAME = "chart";
	private static final String CHART_FILE_EXT = ".jpg";
	private static final String CHART_PREFIX = "";
	private static final String CHART_SUFFIX = "_Futures";

	private ChartBuilder<EventBalanceState> chartBuilder;

	public FuturesBalanceReportGenerator(BalanceVisualizerConfig config, ChartBuilder<EventBalanceState> chartBuilder) {
		super(config);
		this.chartBuilder = chartBuilder;
	}

	@Override
	public BalanceReport getBalanceReport(List<EventBalanceState> balanceStates) throws IOException {
		Collections.sort(balanceStates, Comparator.comparing(EventBalanceState::getDateTime));
		final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

		final List<Asset> assetDataList = balanceStates.stream().flatMap(bal -> bal.getAssets().stream())
				.collect(Collectors.toList());

		final String subject = !isNull(config.getSubject()) ? config.getSubject().get(0) : DEFAULT_CHART_NAME;
		final String chartPath = config.getOutputDir() + "/" + CHART_PREFIX + subject + CHART_SUFFIX  + CHART_FILE_EXT;
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

		final BigDecimal balanceUpdateDelta = calculateBalanceDelta(balanceStates);
		final List<Transaction> transactions = getTransactions(balanceStates);
		return BalanceReport.builder()
				.user(config.getSubject().get(0))
				.transactions(transactions)
				.totalTxCount(transactions.size())
				.startTrackDate(config.getStartTrackDate())
				.finishTrackDate(config.getFinishTrackDate())
				.balanceAtStart(balanceStates.size() != 0 ? balanceStates.get(0).getAssetBalance(VIRTUAL_USD) : BigDecimal.ZERO)
				.balanceAtEnd(balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).getAssetBalance(VIRTUAL_USD) : BigDecimal.ZERO)
				.depositDelta(getDepositDelta(balanceStates))
				.min(findMinBalance(assetDataList))
				.max(findMaxBalance(assetDataList))
				.outputPath(generatedPlotPath)
				.balanceDifference(balanceUpdateDelta).build();
	}

	private List<Transaction> getTransactions(List<EventBalanceState> balanceStates) {
		return balanceStates.stream()
				.flatMap(st -> st.getTXs().stream())
				.collect(Collectors.toList());
	}

	private BigDecimal calculateBalanceDelta(List<EventBalanceState> balanceStates) {
		return balanceStates.size() != 0
				? balanceStates.get(balanceStates.size() - 1).getAssetBalance(VIRTUAL_USD)
				.subtract(balanceStates.get(0).getAssetBalance(VIRTUAL_USD))
				: BigDecimal.ZERO;
	}

	private BigDecimal getDepositDelta(List<EventBalanceState> balanceStates){
		return balanceStates.stream().flatMap(state -> state.getTXs().stream())
				.filter(tx -> transferTxTypes.contains(tx.getType()))
				.map(Transaction::getValueIncome)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	public static String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
		return saveChartToFile(chart, new File(outputFileName));
	}

	public static String saveChartToFile(JFreeChart chart, File file) throws IOException {
		ChartUtils.saveChartAsJPEG(file, chart, 2000, 1000);
		return file.getPath();
	}

	private static BigDecimal findMaxBalance(List<Asset> assetList) {
		return assetList.stream().map(Asset::getBalance).reduce(BigDecimal::max).orElse(BigDecimal.ZERO);
	}

	private static BigDecimal findMinBalance(List<Asset> assetList) {
		return assetList.stream().map(Asset::getBalance).reduce(BigDecimal::min).orElse(BigDecimal.ZERO);
	}
}
