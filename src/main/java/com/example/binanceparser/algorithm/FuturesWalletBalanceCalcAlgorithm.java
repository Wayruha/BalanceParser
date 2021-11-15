package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * this algorithm just uses AccUpdate.walletBalance, without any calculations
 */
public class FuturesWalletBalanceCalcAlgorithm implements CalculationAlgorithm {


    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> allEvents, List<String> assetsToTrack) {

        final List<FuturesAccountUpdateEvent> events = allEvents.stream()
                .map(e -> (FuturesAccountUpdateEvent) e)
                .filter(state -> state.getBalances().stream().anyMatch(bal -> assetsToTrack.contains(bal.getAsset()))).collect(Collectors.toList());

        List<BalanceState> assetList = events.stream().map(e -> {
            final Set<Asset> assets = e.getBalances().stream()
                    .map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getWalletBalance())))
                    .collect(Collectors.toSet());
            return new BalanceState(e.getDate().toLocalDate(), assets);
        }).collect(Collectors.toList());
        return assetList;
    }
}
