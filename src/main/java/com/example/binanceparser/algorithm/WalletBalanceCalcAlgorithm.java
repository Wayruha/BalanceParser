package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.events.*;
import com.example.binanceparser.domain.BalanceState;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class WalletBalanceCalcAlgorithm implements CalculationAlgorithm{


    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<BalanceState> balanceStates = new ArrayList<>();
        Set<BalanceState.Asset> currentBalance = new HashSet<>();
        for(int i = 0; i < events.size() - 1; i++) {
            AbstractEvent abstractEvent = events.get(i);
            AbstractEvent nextAbstractEvent = events.get(i + 1);
            if(abstractEvent.getEventType() != EventType.ORDER_TRADE_UPDATE || nextAbstractEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) continue;
            if(!abstractEvent.getDate().isEqual(nextAbstractEvent.getDate())) continue;

            OrderTradeUpdateEvent event = (OrderTradeUpdateEvent) abstractEvent;
            AccountPositionUpdateEvent accountPositionUpdate = (AccountPositionUpdateEvent) nextAbstractEvent;
            if(!event.getOrderStatus().equals("FILLED")) continue;

            Set<BalanceState.Asset> newEventAssets = accountPositionUpdate.getBalances().stream().map(asset ->
                new BalanceState.Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()))).collect(Collectors.toSet());

            if(balanceStates.size() == 0) currentBalance = newEventAssets;
            balanceStates.add(new BalanceState(nextAbstractEvent.getDate().toLocalDate(),
                    processBalance(currentBalance, newEventAssets)));
        }
        return balanceStates;
    }

    public Set<BalanceState.Asset> processBalance(Set<BalanceState.Asset> finalBalance, Set<BalanceState.Asset> newBalance) {
        Set<BalanceState.Asset> newAsset = new HashSet<>();
            for(BalanceState.Asset asset: newBalance) {
                BalanceState.Asset searchedBalance;
                if (finalBalance.stream().anyMatch(balance -> balance.getAsset().contains(asset.getAsset()))) {
                    searchedBalance = finalBalance.stream().filter(balance ->
                            balance.getAsset().contains(asset.getAsset())).findFirst().get();
                    if (searchedBalance.getAvailableBalance().compareTo(asset.getAvailableBalance()) != 0)
                        searchedBalance.setAvailableBalance(asset.getAvailableBalance());
                } else newAsset.add(asset);
            }
        finalBalance.addAll(newAsset);
        finalBalance = finalBalance.stream().filter(balance ->
                balance.getAvailableBalance().compareTo(BigDecimal.valueOf(0)) != 0).collect(Collectors.toSet());// delete assets with 0 balance
        return finalBalance;
    }
}
