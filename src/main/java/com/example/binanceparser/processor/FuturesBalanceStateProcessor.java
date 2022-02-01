package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.FuturesWalletBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.plot.FuturesBalanceChartBuilder;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.BalanceReportGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.STABLECOIN_RATE;
import static com.example.binanceparser.domain.AccountUpdateReasonType.DEPOSIT;
import static com.example.binanceparser.domain.AccountUpdateReasonType.WITHDRAW;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class FuturesBalanceStateProcessor extends Processor<BalanceVisualizerConfig> {
    final EventSource<AbstractEvent> eventSource;
    final BalanceReportGenerator balanceReportGenerator;
    final FuturesWalletBalanceCalcAlgorithm algorithm;

    public FuturesBalanceStateProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) {
        super(config);
        this.eventSource = eventSource;
        final ChartBuilder<EventBalanceState> chartBuilder = new FuturesBalanceChartBuilder(config.getAssetsToTrack());
        this.balanceReportGenerator = new BalanceReportGenerator(config, chartBuilder);
        this.algorithm = new FuturesWalletBalanceCalcAlgorithm(config, STABLECOIN_RATE);
    }

    @Override
    public BalanceReport process() throws IOException {
        final List<AbstractEvent> events = eventSource.getData().stream()
                .filter(event -> filters(config).stream().allMatch(filter -> filter.filter(event)))
                .collect(Collectors.toList());
        if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");

        // retrieve balance changes
        final List<EventBalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());
        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        System.out.println("Deposit delta: " + calculateDepositDelta(events));
        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }

    public static BigDecimal calculateDepositDelta(List<AbstractEvent> events){
        final List<FuturesAccountUpdateEvent> relevantEvents = events.stream()
                .filter(e -> e instanceof FuturesAccountUpdateEvent)
                .map(e -> (FuturesAccountUpdateEvent) e)
                .filter(e -> e.getReasonType() == DEPOSIT || e.getReasonType() == WITHDRAW)
                .collect(Collectors.toList());

        System.out.println("Logging all Futures DEPOSITs and WITHDRAWs");

        relevantEvents.forEach(e->{
            final String str = String.format("%s Futures %s %s", e.getDateTime().format(ISO_DATE_TIME),
                    e.getReasonType(), e.getBalances().get(0).getBalanceChange());
            System.out.println(str);
        });

        return relevantEvents.stream()
                .map(e -> totalBalanceChange(e.getBalances()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    //TODO refactor. 1. it should not rely on balanceReportGenerator here.
    // 2- cast to different Asset does not look good
    public static BigDecimal totalBalanceChange(List<FuturesAccountUpdateEvent.Asset> balances){
        final Set<Asset> assets = balances.stream()
                .map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getBalanceChange())))
                .collect(Collectors.toSet());
        return FuturesWalletBalanceCalcAlgorithm.totalBalance(assets).orElse(BigDecimal.ZERO);
    }

    //TODO 1st: one filter can be applied at eventSource level
    // 2nd: another filter should be applied at generator level (because SPOT calculator wants to know the state before the target dates)
    private static Set<Filter> filters(BalanceVisualizerConfig config) {
        final Set<Filter> filters = new HashSet<>();
        if (config.getStartTrackDate() != null || config.getFinishTrackDate() != null)
            filters.add(new DateEventFilter(config.getStartTrackDate(), config.getFinishTrackDate()));

        if (config.getSubject() != null) filters.add(new SourceFilter(config.getSubject()));

        if (config.getEventType() != null) filters.add(new EventTypeFilter(config.getEventType()));
        return filters;
    }
}