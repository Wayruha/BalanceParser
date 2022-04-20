package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.FuturesWalletBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.balance.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.plot.FuturesBalanceChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.generator.FuturesBalanceReportGenerator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.example.binanceparser.domain.AccountUpdateReasonType.DEPOSIT;
import static com.example.binanceparser.domain.AccountUpdateReasonType.WITHDRAW;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Service
public class FuturesBalanceProcessor extends Processor<AbstractEvent, BalanceReport> {
    private static final Logger log = Logger.getLogger(FuturesBalanceProcessor.class.getName());
    private final FuturesBalanceReportGenerator balanceReportGenerator;
    private final FuturesWalletBalanceCalcAlgorithm algorithm;
    private final BalanceVisualizerConfig config;

    public FuturesBalanceProcessor(DataSource<AbstractEvent> dataSource, FuturesBalanceReportGenerator balanceReportGenerator,
                                   FuturesWalletBalanceCalcAlgorithm algorithm, BalanceVisualizerConfig config) {
        super(dataSource);
        this.config = config;
        final ChartBuilder<EventBalanceState> chartBuilder = new FuturesBalanceChartBuilder(config.getAssetsToTrack());
        this.balanceReportGenerator = balanceReportGenerator;//new FuturesBalanceReportGenerator(config, chartBuilder);
        this.algorithm = algorithm;//new FuturesWalletBalanceCalcAlgorithm(config, STABLECOIN_RATE);
    }

    @Override
    protected BalanceReport process(List<AbstractEvent> data) {
        final List<AbstractEvent> events = data.stream()
                .filter(event -> filters(config).stream().allMatch(filter -> filter.filter(event)))
                .collect(Collectors.toList());
        if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");

        // retrieve balance changes
        final List<EventBalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());
        BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);
        log.info("FuturesProcessor done for config: " + config);
        log.info("Deposit delta: " + calculateFuturesTransferDelta(events));
        return balanceReport;
    }

    public static BigDecimal calculateFuturesTransferDelta(List<AbstractEvent> events) {
        final List<FuturesAccountUpdateEvent> relevantEvents = events.stream()
                .filter(e -> e instanceof FuturesAccountUpdateEvent)
                .map(e -> (FuturesAccountUpdateEvent) e)
                .filter(e -> e.getReasonType() == DEPOSIT || e.getReasonType() == WITHDRAW)
                .collect(Collectors.toList());

        log.fine("Logging all Futures DEPOSITs and WITHDRAWs");
        relevantEvents.forEach(e -> {
            final String str = String.format("%s Futures %s %s", e.getDateTime().format(ISO_DATE_TIME),
                    e.getReasonType(), e.getBalances().get(0).getBalanceChange());
            log.fine(str);
        });

        return relevantEvents.stream()
                .map(e -> totalFuturesTransfers(e.getBalances()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    //TODO refactor. 1. it should not rely on balanceReportGenerator here.
    // 2- cast to different Asset does not look good
    public static BigDecimal totalFuturesTransfers(List<FuturesAccountUpdateEvent.Asset> balances) {
        final Set<Asset> assets = balances.stream()
                .map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getBalanceChange())))
                .collect(Collectors.toSet());
        return FuturesWalletBalanceCalcAlgorithm.totalBalance(assets).orElse(BigDecimal.ZERO);
    }

    private static Set<Filter> filters(BalanceVisualizerConfig config) {
        final Set<Filter> filters = new HashSet<>();
        if (config.getStartTrackDate() != null || config.getFinishTrackDate() != null)
            filters.add(new DateEventFilter(config.getStartTrackDate(), config.getFinishTrackDate()));

        if (config.getSubjects() != null) filters.add(new SourceFilter(config.getSubjects()));

        if (config.getEventType() != null) filters.add(new EventTypeFilter(config.getEventType()));
        return filters;
    }
}