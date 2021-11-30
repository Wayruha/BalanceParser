package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.FuturesWalletBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.BalanceReportGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.STABLECOIN_RATE;
import static com.example.binanceparser.domain.AccountUpdateReasonType.DEPOSIT;
import static com.example.binanceparser.domain.AccountUpdateReasonType.WITHDRAW;

public class BalanceStateProcessor extends Processor<BalanceVisualizerConfig> {
    final EventSource<AbstractEvent> eventSource;
    final BalanceReportGenerator balanceReportGenerator;
    final FuturesWalletBalanceCalcAlgorithm algorithm;

    public BalanceStateProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) {
        super(config);
        this.eventSource = eventSource;
        this.balanceReportGenerator = new BalanceReportGenerator(config, STABLECOIN_RATE);
        this.algorithm = new FuturesWalletBalanceCalcAlgorithm();
    }

    @Override
    public BalanceReport process() throws IOException {
        final List<AbstractEvent> events = eventSource.getData();
        if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");
        // retrieve balance changes
        final List<EventBalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());
        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        System.out.println("Deposit delta: " + calculateDepositDelta(events));
        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }

    //TODO should be part of report
    private BigDecimal calculateDepositDelta(List<AbstractEvent> events){
        final List<FuturesAccountUpdateEvent> relevantEvents = events.stream()
                .filter(e -> e instanceof FuturesAccountUpdateEvent)
                .map(e -> (FuturesAccountUpdateEvent) e)
                .filter(e -> e.getReasonType() == DEPOSIT || e.getReasonType() == WITHDRAW)
                .collect(Collectors.toList());

        System.out.println("Logging all Futures DEPOSITs and WITHDRAWs");
        relevantEvents.forEach(System.out::println);

        return relevantEvents.stream()
                .map(e -> totalBalanceChange(e.getBalances()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    //TODO refactor. 1. it should not rely on balanceReportGenerator here.
    // 2- cast to different Asset does not look good
    public BigDecimal totalBalanceChange(List<FuturesAccountUpdateEvent.Asset> balances){
        final Set<Asset> assets = balances.stream()
                .map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getBalanceChange())))
                .collect(Collectors.toSet());
        return balanceReportGenerator.totalBalance(assets).orElse(BigDecimal.ZERO);
    }
}
