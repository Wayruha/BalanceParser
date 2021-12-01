package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.USD;


/**
 * this algorithm just uses AccUpdate.walletBalance, without any calculations
 */
public class FuturesWalletBalanceCalcAlgorithm implements CalculationAlgorithm {
    private final BalanceVisualizerConfig config;
    private final Map<String, BigDecimal> currencyRate;

    public FuturesWalletBalanceCalcAlgorithm(BalanceVisualizerConfig config, Map<String, BigDecimal> currencyRate) {
        this.config = config;
        this.currencyRate = currencyRate;
    }

    public List<EventBalanceState> processEvents(List<AbstractEvent> allEvents, List<String> assetsToTrack) {
        List<FuturesAccountUpdateEvent> events = allEvents.stream()
                .filter(e -> e instanceof FuturesAccountUpdateEvent)
                .map(e -> (FuturesAccountUpdateEvent) e)
                .filter(state -> state.getBalances().stream().anyMatch(bal -> assetsToTrack.isEmpty() || assetsToTrack.contains(bal.getAsset())))
                .collect(Collectors.toList());

        List<EventBalanceState> assetList = events.stream().map(e -> {
            final Set<Asset> assets = e.getBalances().stream()
                    .map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getWalletBalance())))
                    .collect(Collectors.toSet());
            return new EventBalanceState(e.getDate().toLocalDate(), assets, false);
        }).collect(Collectors.toList());

        if (config.isConvertToUSD()) {
            calculateUSDCosts(assetList);
        }
        return assetList;
    }

    /**
     * convert all assets to combined $-value
     * Adds a separate asset - "USD"
     */
    private void calculateUSDCosts(List<EventBalanceState> assetList) {
        for (EventBalanceState state : assetList) {
            final Optional<BigDecimal> optBalance = totalBalance(state.getAssets());
            optBalance.ifPresent(bal -> state.getAssets().add(new Asset(USD, bal)));
        }
    }

    public Optional<BigDecimal> totalBalance(Set<Asset> assets) {
        return assets.stream()
                .map(this::assetToUSD)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add);
    }

    public BigDecimal assetToUSD(Asset asset) {
        if (!currencyRate.containsKey(asset.getAsset())) return null;
        return asset.getAvailableBalance().multiply(currencyRate.get(asset.getAsset()));
    }
}
