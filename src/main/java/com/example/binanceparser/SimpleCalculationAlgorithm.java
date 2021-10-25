package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.FuturesAccountUpdateEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.domain.EventType.FUTURES_ACCOUNT_UPDATE;

/**
 * this algorithm just uses AccUpdate.walletBalance, without any calculations
 */
public class SimpleCalculationAlgorithm implements CalculationAlgorithm {
    public static final String ASSET_TO_TRACK = "USDT";

    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> allEvents) {
        //leave only FUTURES_ACCOUNT_UPDATE events
        final List<FuturesAccountUpdateEvent> events = allEvents.stream()
                .filter(e -> e.getEventType() == FUTURES_ACCOUNT_UPDATE)
                .map(e -> (FuturesAccountUpdateEvent) e)
                .filter(e -> e.getBalances().stream().anyMatch(bal -> bal.getAsset().equals(ASSET_TO_TRACK)))
                .collect(Collectors.toList());

        return events.stream().map(e -> {
            final List<BalanceState.Asset> assets = e.getBalances().stream()
                    .map(asset -> new BalanceState.Asset(asset.getAsset(), BigDecimal.valueOf(asset.getWalletBalance())))
                    .collect(Collectors.toList());
            return new BalanceState(e.getDate().toLocalDate(), assets);
        }).collect(Collectors.toList());
    }
}
