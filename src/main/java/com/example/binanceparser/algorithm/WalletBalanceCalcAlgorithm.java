package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.events.*;
import com.example.binanceparser.domain.BalanceState;

import java.util.ArrayList;
import java.util.List;

public class WalletBalanceCalcAlgorithm implements CalculationAlgorithm{



    @Override
    public List<BalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<BalanceState> balanceStates = new ArrayList<>();

        for(int i = 0; i < events.size() - 1; i++) {

            AbstractEvent abstractEvent = events.get(i);
            AbstractEvent nextAbstractEvent = events.get(i + 1);
            BalanceState balanceState = new BalanceState();
            if(!abstractEvent.getEventType().equals(EventType.ORDER_TRADE_UPDATE) && !nextAbstractEvent.getEventType().equals(EventType.ACCOUNT_POSITION_UPDATE)) continue;

            OrderTradeUpdateEvent event = (OrderTradeUpdateEvent) abstractEvent;
            AccountPositionUpdateEvent nextEvent = (AccountPositionUpdateEvent) nextAbstractEvent;
            if(!event.getOrderStatus().equals("Filled")) continue;

        }
        return null;
    }
}
