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
        Set<Asset> currentBalance = new HashSet<>();
        final HashMap<String, BigDecimal> assetRate = new HashMap<>();
        assetRate.put("USDT", BigDecimal.valueOf(1.0)); // because order event has no USDT rate
        for(int i = 0; i < events.size() - 1; i++) {

            AbstractEvent abstractEvent = events.get(i);
            AbstractEvent nextAbstractEvent = events.get(i + 1);
            if(abstractEvent.getEventType() != EventType.ORDER_TRADE_UPDATE ||
                    nextAbstractEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if(!abstractEvent.getDate().isEqual(nextAbstractEvent.getDate())) continue;

            OrderTradeUpdateEvent event = (OrderTradeUpdateEvent) abstractEvent;
            AccountPositionUpdateEvent accountPositionUpdate = (AccountPositionUpdateEvent) nextAbstractEvent;

            if(!assetRate.containsKey(event.getSymbol()))
                assetRate.put(event.getSymbol().replace("USDT", ""),// all assets in order came with USDT suffix
                        event.getPrice());

            if(!event.getOrderStatus().equals("FILLED")) continue;
            Set<Asset> newEventAssets = accountPositionUpdate.getBalances().stream().map(asset ->
                new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());
            currentBalance = processBalance(currentBalance, newEventAssets);
            BalanceState balanceState = new BalanceState();
            balanceState.setAssets(currentBalance);
            balanceState.setDateTime(nextAbstractEvent.getDate().toLocalDate());
            balanceStates.add(balanceState);// every balance state reassign previous balance states

        }
        System.out.println(balanceStates);
        return balanceToUSDT(balanceStates, assetRate);
    }

    public Set<Asset> processBalance(Set<Asset> finalBalance, Set<Asset> newBalance) {
        for(Asset asset: newBalance) {
            Asset searchedBalance;
            if (finalBalance.stream().anyMatch(b -> b.getAsset().contains(asset.getAsset()))) {
                searchedBalance = finalBalance.stream().filter(balance ->
                        balance.getAsset().contains(asset.getAsset())).findFirst().get();

                if (searchedBalance.getAvailableBalance().compareTo(asset.getAvailableBalance()) != 0)
                    searchedBalance.setAvailableBalance(asset.getAvailableBalance());
            }
            else finalBalance.add(asset);
        }
        //System.out.println(assets);
        return finalBalance;
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
