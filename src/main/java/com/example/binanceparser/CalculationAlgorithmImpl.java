package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.FuturesOrderTradeUpdateEvent;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.binanceparser.domain.EventType.FUTURES_ACCOUNT_UPDATE;
import static com.example.binanceparser.domain.EventType.FUTURES_ORDER_TRADE_UPDATE;

public class CalculationAlgorithmImpl implements CalculationAlgorithm {

    public List<BalanceState> processEvents2(List<AbstractEvent> allEvents) {
        List<BalanceState> balances = new ArrayList<>();
        for (AbstractEvent event : allEvents) {
            if (!event.getEventType().getEventTypeId().equals("FUTURES_ACCOUNT_UPDATE")) continue;
            AbstractEvent nextEvent;
            nextEvent = allEvents.get(allEvents.indexOf(event) + 1);


            if (!nextEvent.getEventType().getEventTypeId().equals("FUTURES_ORDER_TRADE_UPDATE")) continue;
            FuturesAccountUpdateEvent accountUpdateEvent = (FuturesAccountUpdateEvent) event;
            FuturesOrderTradeUpdateEvent orderTradeUpdateEvent = (FuturesOrderTradeUpdateEvent) nextEvent;

            if ((!accountUpdateEvent.getReasonType().name().equals("ORDER")) && (orderTradeUpdateEvent.getOrderStatus().equals("FILLED")))
                continue;
            if (!orderTradeUpdateEvent.getSymbol().equals("BTCUSDT")) continue;
            FuturesAccountUpdateEvent.Asset balancesUSDT = accountUpdateEvent.getBalances().stream()
                    .filter(e -> e.getAsset().equals("USDT")).findFirst().orElseThrow(() -> new IllegalStateException("Balance not found!!!"));

            Double currentBalance = balancesUSDT.getCrossWalletBalance();
            if (!orderTradeUpdateEvent.isReduceOnly()) {
                currentBalance += orderTradeUpdateEvent.getOriginalQuantity() / orderTradeUpdateEvent.getPrice();
            }
            BalanceState balanceState = new BalanceState();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            balanceState.setDateTime(accountUpdateEvent.getDate().toLocalDate());
            List<BalanceState.Asset> assetList = new ArrayList<>();
            // TODO BUG here? there could be other assets, not only USDT
            for (FuturesAccountUpdateEvent.Asset balance : accountUpdateEvent.getBalances()) {
                assetList.add(new BalanceState.Asset(balance.getAsset(), BigDecimal.valueOf(balance.getCrossWalletBalance())));
            }
            balanceState.setAssets(assetList);
            //System.out.println(accountUpdateEvent.getDate() + " Balance: " + currentBalance);
            balances.add(balanceState);
        }
        return balances;
    }

    public List<BalanceState> processEvents(List<AbstractEvent> events) {
        final List<BalanceState> balances = new ArrayList<>();
        for (int i = 0; i < events.size() - 1; i++) {
            if (events.get(i).getEventType() != FUTURES_ACCOUNT_UPDATE || events.get(i + 1).getEventType() != FUTURES_ORDER_TRADE_UPDATE)
                continue;
            final FuturesAccountUpdateEvent accUpdate = (FuturesAccountUpdateEvent) events.get(i);
            final FuturesOrderTradeUpdateEvent nextOrderEvent = (FuturesOrderTradeUpdateEvent) events.get(i + 1);

            if (accUpdate.getReasonType().name().equals("ORDER") && !nextOrderEvent.getOrderStatus().equals("FILLED"))
                continue;
            if (!nextOrderEvent.getSymbol().equals("BTCUSDT")) continue;

            Double currentBalance = accUpdate.getBalances().stream()
                    .filter(e -> e.getAsset().equals("USDT")).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Balance not found!!!"))
                    .getCrossWalletBalance();

            if (!nextOrderEvent.isReduceOnly()) {
                final double positionQty = nextOrderEvent.getOriginalQuantity() / nextOrderEvent.getPrice();
                System.out.println(accUpdate.getDate() + " open position: " + positionQty);
                currentBalance += positionQty;
            }

            BalanceState balanceState = new BalanceState();
            balanceState.setDateTime(accUpdate.getDate().toLocalDate());
            List<BalanceState.Asset> assetList = new ArrayList<>();
            for (FuturesAccountUpdateEvent.Asset balance : accUpdate.getBalances()) {
                assetList.add(new BalanceState.Asset(balance.getAsset(), BigDecimal.valueOf(balance.getCrossWalletBalance())));
            }
            balanceState.setAssets(assetList);
            //System.out.println(accUpdate.getDate() + " Balance: " + currentBalance);
            balances.add(balanceState);
        }

        //TODO there still might be last log-element unprocessed!
        return balances;
    }
}
