package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.FuturesOrderTradeUpdateEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.binanceparser.domain.EventType.FUTURES_ACCOUNT_UPDATE;
import static com.example.binanceparser.domain.EventType.FUTURES_ORDER_TRADE_UPDATE;

/**
 * CLASS is not used anymore is the algorithm is not correct.
 * One of the problems is that position qty, used to calculate balance, is created using leverage
 * Therefore, it does not represent the real amount of money spent
 */
public class CalculationAlgorithmImpl implements CalculationAlgorithm {

    final String assetToTrack;

    public CalculationAlgorithmImpl(String assetToTrack) {
        this.assetToTrack = assetToTrack;
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

            Double currentBalance = accUpdate.getBalances().stream()
                    .filter(e -> e.getAsset().equals(assetToTrack)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Balance not found!!!"))
                    .getCrossWalletBalance();

            if (!nextOrderEvent.isReduceOnly()) {
                final double positionQty = nextOrderEvent.getOriginalQuantity() * nextOrderEvent.getPrice();
                System.out.println(accUpdate.getDate() + " open position: " + positionQty);
                currentBalance += positionQty;
            }

            BalanceState balanceState = new BalanceState();
            balanceState.setDateTime(accUpdate.getDate().toLocalDate());
            List<BalanceState.Asset> assetList = new ArrayList<>();
            FuturesAccountUpdateEvent.Asset accUpdateAsset = accUpdate.getBalances().stream().filter(asset -> asset.getAsset().equals(assetToTrack))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Balance not found"));
            assetList.add(new BalanceState.Asset(accUpdateAsset.getAsset(), BigDecimal.valueOf(currentBalance)));
            balanceState.setAssets(assetList);

            balances.add(balanceState);
        }
        return balances;
    }
}
