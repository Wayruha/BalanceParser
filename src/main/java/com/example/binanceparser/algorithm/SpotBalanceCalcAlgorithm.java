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
        final Set<Asset> currentBalance = new HashSet<>();
        final HashMap<String, BigDecimal> assetRate = new HashMap<>();
        assetRate.put("USDT", BigDecimal.valueOf(1)); // because in order events not operations with USDT rate
        for(int i = 0; i < events.size() - 1; i++) {
            AbstractEvent abstractEvent = events.get(i);
            AbstractEvent nextAbstractEvent = events.get(i + 1);
            if(abstractEvent.getEventType() != EventType.ORDER_TRADE_UPDATE ||
                    nextAbstractEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if(!abstractEvent.getDate().isEqual(nextAbstractEvent.getDate())) continue;

            OrderTradeUpdateEvent event = (OrderTradeUpdateEvent) abstractEvent;
            AccountPositionUpdateEvent accountPositionUpdate = (AccountPositionUpdateEvent) nextAbstractEvent;

            if(!assetRate.containsKey(event.getSymbol()))
                assetRate.put(event.getSymbol().replace("USDT", ""),// cause all assets in order came with USDT suffix
                        event.getPrice());// find asset rate

            if(!event.getOrderStatus().equals("FILLED")) continue;
            Set<Asset> newEventAssets = accountPositionUpdate.getBalances().stream().map(asset ->
                new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());

            balanceStates.add(new BalanceState(nextAbstractEvent.getDate().toLocalDate(),
                    processBalance(currentBalance, newEventAssets)));
        }
        return balanceToUSDT(balanceStates, assetRate);
    }

    public Set<Asset> processBalance(Set<Asset> finalBalance, Set<Asset> newBalance) {
        for(Asset asset: newBalance) {
            Asset searchedBalance;
            if (finalBalance.stream().anyMatch(balance -> balance.getAsset().contains(asset.getAsset()))) {
                searchedBalance = finalBalance.stream().filter(balance ->
                        balance.getAsset().contains(asset.getAsset())).findFirst().get();

                if (searchedBalance.getAvailableBalance().compareTo(asset.getAvailableBalance()) != 0)
                    searchedBalance.setAvailableBalance(asset.getAvailableBalance());
            }
            else finalBalance.add(asset);
        }
        finalBalance = finalBalance.stream().filter(balance ->
                balance.getAvailableBalance().compareTo(BigDecimal.valueOf(0)) != 0).collect(Collectors.toSet());// delete assets with 0 balance
        return finalBalance;
    }

    public List<BalanceState> balanceToUSDT(List<BalanceState> balanceStates, HashMap<String, BigDecimal> assetRate) {
        List<BalanceState> updatedBalanceState = new ArrayList<>();

        for(BalanceState balanceState : balanceStates) {
            Set<Asset> assets = new HashSet<>();
            BigDecimal balance = balanceState.getAssets().stream().map(asset ->
                    asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset()))).reduce(BigDecimal.ONE, BigDecimal::add);
            assets.add(new Asset("USDT", balance));
            updatedBalanceState.add(new BalanceState(balanceState.getDateTime(), assets));
        }
        return updatedBalanceState;
    }
}
