package com.example.binanceparser.report.generator;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.domain.*;
import com.example.binanceparser.domain.balance.SpotBalanceState;
import com.example.binanceparser.domain.transaction.Transaction;
import com.example.binanceparser.domain.transaction.TransactionType;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.VIRTUAL_USD;
import static com.example.binanceparser.report.generator.FuturesBalanceReportGenerator.saveChartToFile;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;

@Service
public class SpotBalanceReportGenerator extends AbstractBalanceReportGenerator<SpotBalanceState, BalanceVisualizerConfig> {
    private static EnumSet<TransactionType> tradeTransactionTypes = EnumSet.of(TransactionType.BUY, TransactionType.SELL, TransactionType.CONVERT);

    private static final String DEFAULT_CHART_NAME = "chart";
    private static final String CHART_FILE_EXT = ".jpg";
    private static final String CHART_PREFIX = "Spot_";
    private final ChartBuilder<SpotBalanceState> chartBuilder;

    public SpotBalanceReportGenerator(@Qualifier(BeanNames.SPOT_CONFIG) BalanceVisualizerConfig config, @Qualifier(BeanNames.SPOT_CHART_BUILDER) ChartBuilder<SpotBalanceState> chartBuilder) {
        super(config);
        this.chartBuilder = chartBuilder;
    }

    @Override
    public BalanceReport getBalanceReport(List<SpotBalanceState> balanceStates) {
        balanceStates.sort(Comparator.comparing(SpotBalanceState::getDateTime));
        final JFreeChart lineChart = chartBuilder.buildLineChart(balanceStates);

        final String subject = !isNull(config.getSubjects()) ? config.getSubjects().get(0) : DEFAULT_CHART_NAME;
        final String chartPath = config.getOutputDir() + "/" + CHART_PREFIX + subject + CHART_FILE_EXT;
        final String generatedPlotPath = saveChartToFile(lineChart, chartPath);

        List<BigDecimal> values = balanceStates.stream().map(state -> state.findAsset(VIRTUAL_USD).map(Asset::getBalance).orElse(ZERO))
                .collect(Collectors.toList());

        final List<Transaction> transactions = getTransactions(balanceStates);
        return BalanceReport.builder()
                .user(config.getSubjects().get(0))
                .transactions(transactions)
                .totalTxCount(transactions.size())
                .startTrackDate(config.getStartTrackDate())
                .finishTrackDate(config.getFinishTrackDate())
                .balanceAtStart(getStartBalance(balanceStates))
                .balanceAtEnd(getLastBalance(balanceStates))
                .min(values.stream().reduce(BigDecimal::min).orElse(ZERO))
                .max(values.stream().reduce(BigDecimal::max).orElse(ZERO))
                .outputPath(generatedPlotPath)
                .balanceDifference(calcBalanceDelta(balanceStates))
                .build();
    }

    private BigDecimal getStartBalance(List<SpotBalanceState> balanceStates) {
        return balanceStates.size() > 0 ? balanceStates.get(0).calculateVirtualUSDBalance() : ZERO;
    }

    private BigDecimal calcBalanceDelta(List<SpotBalanceState> balanceStates) {
        if (balanceStates.size() == 0) return ZERO;
        final Asset last = balanceStates.get(balanceStates.size() - 1).findAsset(VIRTUAL_USD).get();
        final Asset first = balanceStates.get(0).findAsset(VIRTUAL_USD).get();
        return last.getBalance().subtract(first.getBalance());
    }

    private BigDecimal getLastBalance(List<SpotBalanceState> balanceStates) {
        return balanceStates.size() != 0 ? balanceStates.get(balanceStates.size() - 1).calculateVirtualUSDBalance()
                : ZERO;
    }

    //TODO це дублюючий код взяти з FuturesBalanceReportGenerator. порефакторити
    private List<Transaction> getTransactions(List<SpotBalanceState> balanceStates) {
        return balanceStates.stream()
                .flatMap(st -> st.getTXs().stream())
                .collect(Collectors.toList());
    }
}