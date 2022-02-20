package com.example.binanceparser.algorithm;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.balance.EventBalanceState;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.domain.events.EventType.FUTURES_ACCOUNT_UPDATE;
import static com.example.binanceparser.domain.events.EventType.FUTURES_ORDER_TRADE_UPDATE;

/**
 * CLASS is not used anymore is the algorithm is not correct.
 * One of the problems is that position qty, used to calculate balance, is created using leverage
 * Therefore, it does not represent the real amount of money spent
 */
public class CalculationAlgorithmImpl implements CalculationAlgorithm<EventBalanceState> {

    final String assetToTrack;

    public CalculationAlgorithmImpl(String assetToTrack) {
        this.assetToTrack = assetToTrack;
    }

    public List<EventBalanceState> processEvents(List<AbstractEvent> events, List<String> assetToTrack) {
        final List<EventBalanceState> balances = new ArrayList<>();
        for (int i = 0; i < events.size() - 1; i++) {
            if (events.get(i).getEventType() != FUTURES_ACCOUNT_UPDATE || events.get(i + 1).getEventType() != FUTURES_ORDER_TRADE_UPDATE)
                continue;
            final FuturesAccountUpdateEvent accUpdate = (FuturesAccountUpdateEvent) events.get(i);
            final FuturesOrderTradeUpdateEvent nextOrderEvent = (FuturesOrderTradeUpdateEvent) events.get(i + 1);

            if (accUpdate.getReasonType().name().equals("ORDER") && nextOrderEvent.getOrderStatus() != OrderStatus.FILLED)
                continue;

            Double currentBalance = accUpdate.getBalances().stream()
                    .filter(asset -> asset.getAsset().equals(assetToTrack)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Balance not found!!!"))
                    .getCrossWalletBalance();

            if (!nextOrderEvent.isReduceOnly()) {
                final double positionQty = nextOrderEvent.getOriginalQuantity().multiply(nextOrderEvent.getPrice()).doubleValue();
                System.out.println(accUpdate.getDateTime() + " open position: " + positionQty);
                currentBalance += positionQty;
            }

            EventBalanceState eventBalanceState = new EventBalanceState();
            eventBalanceState.setDateTime(accUpdate.getDateTime());
            Set<Asset> assetList = new HashSet<>();
            FuturesAccountUpdateEvent.Asset accUpdateAsset = accUpdate.getBalances().stream().filter(asset -> asset.getAsset().equals(assetToTrack))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Balance not found"));
            assetList.add(new Asset(accUpdateAsset.getAsset(), BigDecimal.valueOf(currentBalance)));
            eventBalanceState.setAssets(assetList);

            balances.add(eventBalanceState);
        }
        return balances;
    }

	@Override
	public List<EventBalanceState> processEvents(List<AbstractEvent> events) {
		// TODO Auto-generated method stub
		return new ArrayList<EventBalanceState>();
	}
}
