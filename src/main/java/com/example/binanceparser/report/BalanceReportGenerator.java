package com.example.binanceparser.report;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.plot.ChartBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static com.example.binanceparser.Constants.*;
import static java.util.Objects.isNull;

public class BalanceReportGenerator extends AbstractBalanceReportGenerator<EventBalanceState, BalanceVisualizerConfig> {
	private static final String DEFAULT_CHART_NAME = "chart";
	private static final String CHART_FILE_EXT = ".jpg";

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
		final String chartPath = config.getOutputDir() + "/" + subject + CHART_FILE_EXT;
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);
		
		//final BigDecimal delta = calculateBalanceDelta(assetDataList);
		final BigDecimal balanceUpdateDelta = findBalanceUpdateDelta(balanceStates);
		System.out.println(balanceUpdateDelta);
		return BalanceReport.builder().startTrackDate(config.getStartTrackDate())
				.finishTrackDate(config.getFinishTrackDate())
				.balanceAtStart(balanceStates.size() != 0 ? balanceStates.get(0).getAssetBalance(VIRTUAL_USD) : BigDecimal.ZERO)
				.balanceAtEnd(
						balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).getAssetBalance(VIRTUAL_USD)
								: BigDecimal.ZERO)
				.min(findMinBalance(assetDataList)).max(findMaxBalance(assetDataList)).outputPath(generatedPlotPath)
				.balanceDifference(balanceUpdateDelta).build();
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
