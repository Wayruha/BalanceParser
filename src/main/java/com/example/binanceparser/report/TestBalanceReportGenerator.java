package com.example.binanceparser.report;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.plot.ChartBuilder;
import org.jfree.chart.JFreeChart;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.VIRTUAL_USD;
import static com.example.binanceparser.report.BalanceReportGenerator.saveChartToFile;
import static java.util.Objects.isNull;

public class TestBalanceReportGenerator extends AbstractBalanceReportGenerator<SpotIncomeState, BalanceVisualizerConfig> {
    private static final String DEFAULT_CHART_NAME = "chart";
    private static final String CHART_FILE_EXT = ".jpg";
    private final ChartBuilder<SpotIncomeState> chartBuilder;

    public TestBalanceReportGenerator(BalanceVisualizerConfig config, ChartBuilder<SpotIncomeState> chartBuilder) {
        super(config);
        this.chartBuilder = chartBuilder;
    }

    @Override
    public BalanceReport getBalanceReport(List<SpotIncomeState> balanceStates) throws IOException {
        balanceStates.sort(Comparator.comparing(SpotIncomeState::getDateTime));
        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

        final String subject = !isNull(config.getSubject()) ? config.getSubject().get(0) : DEFAULT_CHART_NAME;
        final String chartPath = config.getOutputDir() + "/" + "TEST_" + subject + CHART_FILE_EXT;
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

        List<BigDecimal> values = balanceStates.stream().map(state -> state.findAsset(VIRTUAL_USD).map(Asset::getBalance).orElse(BigDecimal.ZERO))
                .collect(Collectors.toList());

        return BalanceReport.builder().startTrackDate(config.getStartTrackDate())
                .finishTrackDate(config.getFinishTrackDate())
                .balanceAtStart(balanceStates.get(0).calculateVirtualUSDBalance())
                .balanceAtEnd(getLastBalance(balanceStates))
                .min(values.stream().reduce(BigDecimal::min).orElse(BigDecimal.ZERO))
                .max(values.stream().reduce(BigDecimal::max).orElse(BigDecimal.ZERO))
                .outputPath(generatedPlotPath)
                .balanceDifference(calcBalanceDelta(balanceStates))
                .build();
    }

    private BigDecimal calcBalanceDelta(List<SpotIncomeState> balanceStates) {
        if (balanceStates.size() == 0) return BigDecimal.ZERO;
        final Asset last = balanceStates.get(balanceStates.size() - 1).findAsset(VIRTUAL_USD).get();
        final Asset first = balanceStates.get(0).findAsset(VIRTUAL_USD).get();
        return last.getBalance().subtract(first.getBalance());
    }

    private BigDecimal getLastBalance(List<SpotIncomeState> balanceStates) {
        return balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).calculateVirtualUSDBalance()
                : BigDecimal.ZERO;
    }
}