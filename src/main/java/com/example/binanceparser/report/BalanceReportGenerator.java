package com.example.binanceparser.report;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.*;
import com.example.binanceparser.plot.ChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static com.example.binanceparser.Constants.*;
import static java.util.Objects.isNull;

public class BalanceReportGenerator extends AbstractBalanceReportGenerator<EventBalanceState, BalanceVisualizerConfig> {
	private static EnumSet<TransactionType> tradeTransactionTypes = EnumSet.of(TransactionType.BUY, TransactionType.SELL, TransactionType.CONVERT);
	private static final String DEFAULT_CHART_NAME = "chart";
	private static final String CHART_FILE_EXT = ".jpg";
	private static final String CHART_PREFIX = "Futures_";

	private ChartBuilder<EventBalanceState> chartBuilder;

	public BalanceReportGenerator(BalanceVisualizerConfig config, ChartBuilder<EventBalanceState> chartBuilder) {
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
		final String chartPath = config.getOutputDir() + "/" + CHART_PREFIX + subject + CHART_FILE_EXT;
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

		final BigDecimal balanceUpdateDelta = findBalanceUpdateDelta(balanceStates);
		final List<TransactionX> transactions = getTransactions(balanceStates);
		return BalanceReport.builder()
				.user(config.getSubject().get(0))
				.transactions(transactions)
				.totalTxCount(transactions.size())
				.totalTradeTxCount((int) transactions.stream().filter(tx -> tradeTransactionTypes.contains(tx.getType())).count())
				.startTrackDate(config.getStartTrackDate())
				.finishTrackDate(config.getFinishTrackDate())
				.balanceAtStart(balanceStates.size() != 0 ? balanceStates.get(0).getAssetBalance(VIRTUAL_USD) : BigDecimal.ZERO)
				.balanceAtEnd(balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).getAssetBalance(VIRTUAL_USD) : BigDecimal.ZERO)
				.min(findMinBalance(assetDataList))
				.max(findMaxBalance(assetDataList))
				.outputPath(generatedPlotPath)
				.balanceDifference(balanceUpdateDelta).build();
	}

	private List<TransactionX> getTransactions(List<EventBalanceState> balanceStates) {
		return balanceStates.stream()
				.flatMap(st -> st.getTXs().stream())
				.collect(Collectors.toList());
	}

	private BigDecimal findBalanceUpdateDelta(List<EventBalanceState> balanceStates) {
		return balanceStates.size() != 0
				? balanceStates.get(balanceStates.size() - 1).getAssetBalance(VIRTUAL_USD)
				.subtract(balanceStates.get(0).getAssetBalance(VIRTUAL_USD))
				: BigDecimal.ZERO;
	}

	private BigDecimal calculateBalanceDelta(List<Asset> assetList) {
		if(assetList.size() == 0) return BigDecimal.ZERO;
		final Asset lastAsset = assetList.get(assetList.size() - 1);
		final Asset firstAsset = assetList.get(0);
		return lastAsset.getBalance().subtract(firstAsset.getBalance());
	}

	public static String saveChartToFile(JFreeChart chart, String outputFileName) throws IOException {
		File file = new File(outputFileName);
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
