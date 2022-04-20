package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.balance.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent.Position;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.STABLECOIN_RATE;
import static com.example.binanceparser.Constants.USD;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/*
 * this algorithm just uses AccUpdate.walletBalance, without any calculations
 */
@Service
public class FuturesWalletBalanceCalcAlgorithm implements CalculationAlgorithm<EventBalanceState> {
    private static final Logger LOGGER = Logger.getLogger(FuturesWalletBalanceCalcAlgorithm.class.getName());
    private final BalanceVisualizerConfig config;

    public FuturesWalletBalanceCalcAlgorithm(@Qualifier(BeanNames.FUTURES_CONFIG) BalanceVisualizerConfig config
                                             //, Map<String, BigDecimal> currencyRate
    ) {
        this.config = config;
    }

    @Override
    public List<EventBalanceState> processEvents(List<AbstractEvent> allEvents, List<String> assetsToTrack) {
        List<AbstractEvent> events = allEvents.stream()
                .filter(e -> e instanceof FuturesAccountUpdateEvent || e instanceof FuturesOrderTradeUpdateEvent)
                .collect(Collectors.toList());

        List<EventBalanceState> states = new ArrayList<>();
        EventBalanceState lastState = null;

        for (AbstractEvent event : events) {
            if (event instanceof FuturesOrderTradeUpdateEvent) {
                if (lastState != null)
                    lastState.processTradeEvent((FuturesOrderTradeUpdateEvent) event);
                continue;
            }

            FuturesAccountUpdateEvent accUpdEvent = (FuturesAccountUpdateEvent) event;
            logBalanceUpdate(accUpdEvent);
            final List<Asset> assets = accUpdEvent.getBalances().stream()
                    .map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getWalletBalance())))
                    .collect(Collectors.toList());

            EventBalanceState state = states.size() == 0 ? new EventBalanceState(accUpdEvent.getDateTime())
                    : new EventBalanceState(accUpdEvent.getDateTime(), states.get(states.size() - 1));

            state.updateAssets(assets);
            accUpdEvent.getBalances().forEach(bal -> state.processAccUpdate(accUpdEvent));

            lastState = state;
            states.add(state);
        }
        if (config.isConvertToUSD()) {
            calculateUSDCosts(states);
        }
        return states;
    }

    private void logBalanceUpdate(FuturesAccountUpdateEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getDateTime().format(ISO_DATE_TIME)).append(" ")
                .append(event.getReasonType()).append(" balances:{");
        for (FuturesAccountUpdateEvent.Asset asset : event.getBalances()) {
            sb.append("asset: ").append(asset.getAsset())
                    .append(", wallet balance:").append(asset.getWalletBalance())
                    .append(", balance change:").append(asset.getBalanceChange());
        }
        sb.append("} positions:{");
        for (Position pos : event.getPositions()) {
            sb.append("symbol: ").append(pos.getSymbol())
                    .append(", position amount:").append(pos.getPositionAmount())
                    .append(", entry price:").append(pos.getEntryPrice());
        }
        sb.append("}");
        LOGGER.fine(sb.toString());
    }

    /**
     * convert all assets to combined $-value Adds a separate asset - "USD"
     */
    private void calculateUSDCosts(List<EventBalanceState> assetList) {
        for (EventBalanceState state : assetList) {
            final Optional<BigDecimal> optBalance = totalBalance(state.getAssets());
            optBalance.ifPresent(bal -> state.getAssets().add(new Asset(USD, bal)));
        }
    }

    public static Optional<BigDecimal> totalBalance(Set<Asset> assets) {
        return assets.stream().map(FuturesWalletBalanceCalcAlgorithm::assetToUSD).filter(Objects::nonNull).reduce(BigDecimal::add);
    }

    public static BigDecimal assetToUSD(Asset asset) {
        if (!STABLECOIN_RATE.containsKey(asset.getAsset()))
            return null;
        return asset.getBalance().multiply(STABLECOIN_RATE.get(asset.getAsset()));
    }

    @Override
    public List<EventBalanceState> processEvents(List<AbstractEvent> events) {
        return processEvents(events, config.getAssetsToTrack());
    }
}