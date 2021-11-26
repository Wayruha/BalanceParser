package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.*;
import com.example.binanceparser.domain.EventBalanceState;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class SpotBalanceCalcAlgorithm implements CalculationAlgorithm{

    final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;

    @Override
    public List<EventBalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<EventBalanceState> eventBalanceStates = new ArrayList<>();
        Map<String, Asset> actualBalance = new HashMap<>();
        final HashMap<String, BigDecimal> assetRate = new HashMap<>();
        assetRate.put("USDT", BigDecimal.valueOf(1.0)); // because order event has no USDT rate
        for(int i = 0; i < events.size() - 1; i++) {

            final AbstractEvent firstEvent = events.get(i);
            final AbstractEvent nextEvent = events.get(i + 1);
            if((firstEvent.getEventType() != EventType.ORDER_TRADE_UPDATE && firstEvent.getEventType() != EventType.BALANCE_UPDATE) ||
                    nextEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if(ChronoUnit.SECONDS.between(firstEvent.getDate(), nextEvent.getDate()) > MAX_SECONDS_DELAY_FOR_VALID_EVENTS) continue;

            if(firstEvent.getEventType() == EventType.BALANCE_UPDATE){
                eventBalanceStates.add(processBalanceUpdate(nextEvent, actualBalance));
                continue;
            }

            final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) firstEvent;
            final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

            if(!orderEvent.getOrderStatus().equals("FILLED")) continue;

            Set<Asset> newEventAssets = accEvent.getBalances().stream().map(asset ->
                    new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());

            final String orderSymbol = orderEvent.getSymbol().replace("USDT", "");

            if(orderEvent.getSide().equals("BUY") && assetRate.containsKey(orderSymbol)) {
                BigDecimal newQuantity = BigDecimal.valueOf(orderEvent.getOriginalQuantity());
                BigDecimal currentQuantity = actualBalance.get(orderSymbol).getAvailableBalance();
                BigDecimal newPrice = orderEvent.getPriceOfLastFilledTrade();
                BigDecimal currentPrice = assetRate.get(orderSymbol);
                BigDecimal sum = newQuantity.multiply(newPrice).add(currentQuantity
                        .multiply(currentPrice));
                assetRate.put(orderSymbol, sum.divide(newQuantity.add(currentQuantity), 2));
            }
            else assetRate.put(orderSymbol, orderEvent.getPriceOfLastFilledTrade());

            actualBalance = processBalance(actualBalance, newEventAssets);
            EventBalanceState eventBalanceState = new EventBalanceState(accEvent.getDate().toLocalDate(),
                    new HashSet<>(actualBalance.values()), false);
            eventBalanceStates.add(eventBalanceState);
        }
        return balanceToUSDT(eventBalanceStates, assetRate);
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

    public List<EventBalanceState> balanceToUSDT(List<EventBalanceState> eventBalanceStates, HashMap<String, BigDecimal> assetRate) {
        List<EventBalanceState> updatedEventBalanceState = new ArrayList<>();
        for(EventBalanceState eventBalanceState : eventBalanceStates) {
            Set<Asset> assets = new HashSet<>();
            BigDecimal balance = new BigDecimal(0);
            for(Asset asset: eventBalanceState.getAssets()) {
                if(assetRate.get(asset.getAsset()) == null) balance = balance.add(asset.getAvailableBalance());
                else balance = balance.add(asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset())));
            }
            assets.add(new Asset("USDT", balance));
            updatedEventBalanceState.add(new EventBalanceState(eventBalanceState.getDateTime(), assets, eventBalanceState.isBalanceUpdate()));
        }
        //System.out.println(updatedBalanceState);
        return updatedEventBalanceState;
    }
}
