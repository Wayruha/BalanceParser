package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.valueOf;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class SpotBalanceCalcAlgorithm implements CalculationAlgorithm {
    private final BalanceVisualizerConfig config;
    final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;
    private final HashMap<String, BigDecimal> assetRate;


    public SpotBalanceCalcAlgorithm(BalanceVisualizerConfig config) {
        this.config = config;
        this.assetRate = new HashMap<>();
        assetRate.put(USDT, valueOf(1.0)); // because order event has no USDT rate
        assetRate.put(BUSD, valueOf(1.0)); // because order event has no BUSD rate
    }

    /**
     * assetsToTrack is not used here
     */
    @Override
    public List<EventBalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<EventBalanceState> eventBalanceStates = new ArrayList<>();
        Map<String, Asset> actualBalance = new HashMap<>();
        for (int i = 0; i < events.size() - 1; i++) {
            final AbstractEvent firstEvent = events.get(i);
            final AbstractEvent nextEvent = events.get(i + 1);
            if ((firstEvent.getEventType() != EventType.ORDER_TRADE_UPDATE && firstEvent.getEventType() != EventType.BALANCE_UPDATE) ||
                    nextEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if (ChronoUnit.SECONDS.between(firstEvent.getDate(), nextEvent.getDate()) > MAX_SECONDS_DELAY_FOR_VALID_EVENTS)
                continue;

            if (firstEvent.getEventType() == EventType.BALANCE_UPDATE) {
                eventBalanceStates.add(processBalanceUpdate(nextEvent, actualBalance));
                final BalanceUpdateEvent balUpdate = (BalanceUpdateEvent) firstEvent;
                logBalUpdate(balUpdate);
                continue;
            }

            final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) firstEvent;
            final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;
            if (!orderEvent.getOrderStatus().equals("FILLED")) continue;
            logTrade(orderEvent);

            Set<Asset> newEventAssets = accEvent.getBalances().stream().map(asset ->
                    new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());
            final String orderSymbol = orderEvent.getSymbol().replace(USDT, "");


            if (orderEvent.getSide().equals("BUY") && assetRate.containsKey(orderSymbol)) {
                BigDecimal newQuantity = valueOf(orderEvent.getOriginalQuantity());
                BigDecimal currentQuantity = actualBalance.get(orderSymbol).getAvailableBalance();
                BigDecimal newPrice = orderEvent.getPriceOfLastFilledTrade();
                BigDecimal currentPrice = assetRate.get(orderSymbol);
                final BigDecimal quoteAssetQty = newQuantity.multiply(newPrice);
                final BigDecimal existingQuoteQty = currentQuantity
                        .multiply(currentPrice);
                BigDecimal newTotalAssetQty = quoteAssetQty.add(existingQuoteQty);
                assetRate.put(orderSymbol, newTotalAssetQty.divide(newQuantity.add(currentQuantity), 2));
            } else assetRate.put(orderSymbol, orderEvent.getPriceOfLastFilledTrade());

            actualBalance = processBalance(actualBalance, newEventAssets);
            EventBalanceState eventBalanceState = new EventBalanceState(accEvent.getDate().toLocalDate(),
                    new HashSet<>(actualBalance.values()), false);
            eventBalanceStates.add(eventBalanceState);
        }
        return balanceToUSDT(eventBalanceStates);
    }

    //TODO logging should not be part of Calculation Algorithm. Instead, move it to upper-level or to generator
    private void logBalUpdate(BalanceUpdateEvent balUpdate) {
        System.out.println(balUpdate.getDate().format(ISO_DATE_TIME) + " Balance updated:" + balUpdate.getBalances() + ". Delta=" + balUpdate.getBalanceDelta());
    }

    private void logTrade(OrderTradeUpdateEvent orderEvent) {
        final BigDecimal quoteAssetQty = valueOf(orderEvent.getOriginalQuantity()).multiply(orderEvent.getPriceOfLastFilledTrade());
        final String str = String.format("%s %s %s %s for total of %s quoteAsset", orderEvent.getDate().format(ISO_DATE_TIME), orderEvent.getSide(), valueOf(orderEvent.getOriginalQuantity()).toPlainString(), orderEvent.getSymbol(),
                quoteAssetQty.toPlainString());
        System.out.println(str);
    }

    public EventBalanceState processBalanceUpdate(AbstractEvent nextEvent, Map<String, Asset> actualBalance) {
        final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;
        Set<Asset> newEventAssets = accEvent.getBalances().stream().map(asset ->
                new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());

        actualBalance = processBalance(actualBalance, newEventAssets);
        return new EventBalanceState(accEvent.getDate().toLocalDate(),
                new HashSet<>(actualBalance.values()), true);
    }

    public Map<String, Asset> processBalance(Map<String, Asset> actualBalance, Set<Asset> newBalance) {
        newBalance.forEach(asset -> actualBalance.put(asset.getAsset(), asset));
        return actualBalance;
    }

    public List<EventBalanceState> balanceToUSDT(List<EventBalanceState> eventBalanceStates) {
        List<EventBalanceState> updatedEventBalanceState = new ArrayList<>();
        for (EventBalanceState state : eventBalanceStates) {
            Set<Asset> assets = new HashSet<>();
            BigDecimal balance = new BigDecimal(0);
            for (Asset asset : state.getAssets()) {
                if (assetRate.get(asset.getAsset()) == null) balance = balance.add(asset.getAvailableBalance());
                else balance = balance.add(asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset())));
            }
            assets.add(new Asset(USD, balance));
            updatedEventBalanceState.add(new EventBalanceState(state.getDateTime(), assets, state.isBalanceUpdate()));
        }
        //System.out.println(updatedBalanceState);
        return updatedEventBalanceState;
    }
}
