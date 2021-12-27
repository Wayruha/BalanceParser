package com.example.binanceparser.report;

import static com.example.binanceparser.Constants.USD;
import static com.example.binanceparser.report.BalanceReportGenerator.saveChartToFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.JFreeChart;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.plot.TestAssetChartBuilder;

public class TestBalanceReportGenerator extends AbstractBalanceReportGenerator<SpotIncomeState, BalanceVisualizerConfig> {
	private final ChartBuilder<SpotIncomeState> chartBuilder;

	public TestBalanceReportGenerator(BalanceVisualizerConfig config) {
		super(config);
		List<String> assetsToTrack = config.isConvertToUSD() ? List.of(USD) : config.getAssetsToTrack();
		chartBuilder = new TestAssetChartBuilder(assetsToTrack);
	}

	@Override
	public BalanceReport getBalanceReport(List<SpotIncomeState> balanceStates) throws IOException {
		balanceStates.sort(Comparator.comparing(SpotIncomeState::getDateTime));
		final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

		final String chartPath = config.getOutputDir() + "/" + "TEST_" + config.getSubject().get(0) + ".jpg";
		final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

		List<BigDecimal> values = balanceStates.stream().map(BalanceState::getBalanceState).collect(Collectors.toList());

		return BalanceReport.builder()
				.startTrackDate(config.getStartTrackDate())
				.finishTrackDate(config.getFinishTrackDate())
				.balanceAtStart(BigDecimal.ZERO)
				.balanceAtEnd(balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).getBalanceState()
						: BigDecimal.ZERO)
				.min(values.stream().reduce(BigDecimal::min).orElse(BigDecimal.ZERO))
				.max(values.stream().reduce(BigDecimal::max).orElse(BigDecimal.ZERO))
				.outputPath(generatedPlotPath)
				.balanceDifference(
						balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).getBalanceState()  //TODO чого balanceState? На початок проміжку в нас могло вже бути 1к на балансі
								: BigDecimal.ZERO)
				.build();
	}
}
