package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.FuturesAccountUpdateEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.domain.EventType.FUTURES_ACCOUNT_UPDATE;

/**
 * this algorithm just uses AccUpdate.walletBalance, without any calculations
 */
public class WalletBalanceCalcAlgorithm implements CalculationAlgorithm {


    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> allEvents, List<String> assetsToTrack) {
        //leave only FUTURES_ACCOUNT_UPDATE events
        final List<FuturesAccountUpdateEvent> events = new ArrayList<>();
        for (String asset: assetsToTrack) {
            events.addAll(allEvents.stream()
                    .filter(e -> e.getEventType() == FUTURES_ACCOUNT_UPDATE)
                    .map(e -> (FuturesAccountUpdateEvent) e)
                    .filter(e -> e.getBalances().stream().anyMatch(bal -> bal.getAsset().equals(asset)))
                    .collect(Collectors.toList()));
        }
        events.forEach(System.out::println);

        List<BalanceState> assetList = events.stream().map(e -> {
            final List<BalanceState.Asset> assets = e.getBalances().stream()
                    .map(asset -> new BalanceState.Asset(asset.getAsset(), BigDecimal.valueOf(asset.getWalletBalance())))
                    .collect(Collectors.toList());
            return new BalanceState(e.getDate().toLocalDate(), assets);
        }).collect(Collectors.toList());
        return assetList;
    }
}
