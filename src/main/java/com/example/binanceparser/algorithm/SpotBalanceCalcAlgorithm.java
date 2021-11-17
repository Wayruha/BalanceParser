package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.*;
import com.example.binanceparser.domain.BalanceState;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class SpotBalanceCalcAlgorithm implements CalculationAlgorithm{

    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<BalanceState> balanceStates = new ArrayList<>();
        Map<String, Asset> actualBalance = new HashMap<>();
        final HashMap<String, BigDecimal> assetRate = new HashMap<>();
        assetRate.put("USDT", BigDecimal.valueOf(1.0)); // because order event has no USDT rate
        for(int i = 0; i < events.size() - 1; i++) {

            final AbstractEvent firstEvent = events.get(i);
            final AbstractEvent nextEvent = events.get(i + 1);
            if(firstEvent.getEventType() != EventType.ORDER_TRADE_UPDATE ||
                    nextEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if(ChronoUnit.SECONDS.between(firstEvent.getDate(), nextEvent.getDate()) > 1) continue;

            final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) firstEvent;
            final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

            if(orderEvent.getPrice() == null) System.out.println("Price is null " + orderEvent.getDate());

            String orderSymbol = orderEvent.getSymbol().replace("USDT", "");

            if(assetRate.containsKey(orderSymbol)) {
                System.out.println(assetRate.get(orderSymbol) + " + " + orderEvent.getPriceOfLastFilledTrade());
                assetRate.put(orderSymbol, assetRate.get(orderSymbol)
                        .add(orderEvent.getPriceOfLastFilledTrade()).divide(BigDecimal.valueOf(2)));
            }
            else assetRate.put(orderSymbol,
                        orderEvent.getPriceOfLastFilledTrade());

            System.out.println(assetRate);

            if(!orderEvent.getOrderStatus().equals("FILLED")) continue;
            Set<Asset> newEventAssets = accEvent.getBalances().stream().map(asset ->
                new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());
            actualBalance = processBalance(actualBalance, newEventAssets);
            BalanceState balanceState = new BalanceState(accEvent.getDate().toLocalDate()
                    , new HashSet<>(actualBalance.values()));
            balanceStates.add(balanceState);

        }
        System.out.println(balanceStates);
        return balanceToUSDT(balanceStates, assetRate);
    }

    public Map<String, Asset> processBalance(Map<String, Asset> actualBalance, Set<Asset> newBalance) {
        newBalance.forEach(asset -> actualBalance.put(asset.getAsset(), asset));
        return actualBalance;
    }

    public List<BalanceState> balanceToUSDT(List<BalanceState> balanceStates, HashMap<String, BigDecimal> assetRate) {
        List<BalanceState> updatedBalanceState = new ArrayList<>();
        for(BalanceState balanceState : balanceStates) {
            Set<Asset> assets = new HashSet<>();
            /*BigDecimal balance = new BigDecimal(0);
            balance = balanceState.getAssets().stream().map(asset ->
                    asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset()))).reduce(BigDecimal.ZERO, BigDecimal::add);*/
            BigDecimal balance = new BigDecimal(0);
            for(Asset asset: balanceState.getAssets()) {
                if(assetRate.get(asset.getAsset()) == null) balance = balance.add(asset.getAvailableBalance());
                else balance = balance.add(asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset())));
            }
            assets.add(new Asset("USDT", balance));
            updatedBalanceState.add(new BalanceState(balanceState.getDateTime(), assets));
        }
        return updatedBalanceState;
    }
}
