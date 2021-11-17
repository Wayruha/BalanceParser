package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.*;
import com.example.binanceparser.domain.BalanceState;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class SpotBalanceCalcAlgorithm implements CalculationAlgorithm{

    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<BalanceState> balanceStates = new ArrayList<>();
        Set<Asset> actualBalance = new HashSet<>(); // TODO use hashMap
        final HashMap<String, BigDecimal> assetRate = new HashMap<>();
        assetRate.put("USDT", BigDecimal.valueOf(1.0)); // because order event has no USDT rate
        for(int i = 0; i < events.size() - 1; i++) {

            AbstractEvent firstEvent = events.get(i);
            AbstractEvent nextEvent = events.get(i + 1);
            if(firstEvent.getEventType() != EventType.ORDER_TRADE_UPDATE ||
                    nextEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if(!firstEvent.getDate().isEqual(nextEvent.getDate())) continue; // TODO Вони можуть відрізнятися на мілісекунду

            OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) firstEvent;
            AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

            if(!assetRate.containsKey(orderEvent.getSymbol()))
                assetRate.put(orderEvent.getSymbol().replace("USDT", ""),// all assets in order came with USDT suffix
                        orderEvent.getPrice()); //TODO do we really have price?

            if(!orderEvent.getOrderStatus().equals("FILLED")) continue;
            Set<Asset> newEventAssets = accEvent.getBalances().stream().map(asset ->
                new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());
            actualBalance = processBalance(actualBalance, newEventAssets);
            BalanceState balanceState = new BalanceState();
            balanceState.setAssets(actualBalance);
            balanceState.setDateTime(nextEvent.getDate().toLocalDate());
            balanceStates.add(balanceState);// every balance state reassign previous balance states

        }
        System.out.println(balanceStates);
        return balanceToUSDT(balanceStates, assetRate);
    }

    public Set<Asset> processBalance(Set<Asset> actualBalance, Set<Asset> newBalance) {
        Map<String, Asset> actualBal = new HashMap<>();

        //TODO repalce with Java Stream
        for (Asset newAsset : newBalance) {
            actualBal.put(newAsset.getAsset(), newAsset);
        }

        //System.out.println(assets);
        return actualBalance;
    }

    public List<BalanceState> balanceToUSDT(List<BalanceState> balanceStates, HashMap<String, BigDecimal> assetRate) {
        List<BalanceState> updatedBalanceState = new ArrayList<>();
        for(BalanceState balanceState : balanceStates) {
            Set<Asset> assets = new HashSet<>();
/*            BigDecimal balance = balanceState.getAssets().stream().map(asset ->
                    asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset()))).reduce(BigDecimal::add).orElseThrow();*/
            BigDecimal balance = new BigDecimal(0);
            for(Asset asset: balanceState.getAssets()){
                if(assetRate.get(asset.getAsset()) == null) balance = balance.add(asset.getAvailableBalance());
                else balance = balance.add(asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset())));
                System.out.println(asset.getAsset() + ": " + asset.getAvailableBalance() + " * " + assetRate.get(asset.getAsset()));
            }

            assets.add(new Asset("USDT", balance));
            updatedBalanceState.add(new BalanceState(balanceState.getDateTime(), assets));
        }
        return updatedBalanceState;
    }
}
