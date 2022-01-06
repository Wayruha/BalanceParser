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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.example.binanceparser.Constants.USD;

public class BalanceReportGenerator extends AbstractBalanceReportGenerator<EventBalanceState, BalanceVisualizerConfig> {
	private ChartBuilder<EventBalanceState> chartBuilder;
	
	public BalanceReportGenerator(BalanceVisualizerConfig config) {
		super(config);
		List<String> assetsToTrack = config.getAssetsToTrack();
		chartBuilder = new AssetChartBuilder(assetsToTrack);
	}

	@Override
	public BalanceReport getBalanceReport(List<EventBalanceState> balanceStates) throws IOException {
		Collections.sort(balanceStates, Comparator.comparing(EventBalanceState::getDateTime));
		final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

		final List<Asset> assetList = balanceStates.stream().flatMap(bal -> bal.getAssets().stream())
				.collect(Collectors.toList());

		final String chartPath = new StringBuilder().append(config.getOutputDir()).append("/")
				.append(config.getSubject().get(0)).append(".jpg").toString();
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);
		final BigDecimal delta = calculateBalanceDelta(assetList);
		final BigDecimal balanceUpdateDelta = findBalanceUpdateDelta(balanceStates);
		System.out.println(balanceUpdateDelta);
		return BalanceReport.builder().startTrackDate(config.getStartTrackDate())
				.finishTrackDate(config.getFinishTrackDate())
				.balanceAtStart(balanceStates.size() != 0 ? balanceStates.get(0).getAssetBalance(USD) : BigDecimal.ZERO)
				.balanceAtEnd(
						balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).getAssetBalance(USD)
								: BigDecimal.ZERO)
				.min(findMinBalance(assetList)).max(findMaxBalance(assetList)).outputPath(generatedPlotPath)
				.balanceDifference(delta).build();
	}

	private BigDecimal findBalanceUpdateDelta(List<EventBalanceState> balanceStates) {
		BigDecimal delta = balanceStates.stream().map(EventBalanceState::getBalanceState).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return delta;
	}

	private BigDecimal calculateBalanceDelta(List<Asset> assetList) {
		return assetList.size() != 0 ? assetList.get(assetList.size() - 1).getBalance()
				.add(assetList.get(0).getBalance().negate()) : BigDecimal.ZERO;
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
