package com.example.binanceparser.algorithm;

import com.example.binanceparser.Constants;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.USDT;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class TestSpotBalanceCalcAlgorithm implements CalculationAlgorithm<SpotIncomeState> {
    private final BalanceVisualizerConfig config;
    private final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;

    public TestSpotBalanceCalcAlgorithm(BalanceVisualizerConfig config) {
        this.config = config;
    }

    @Override
    public List<SpotIncomeState> processEvents(List<AbstractEvent> events) {
        return processEvents(events, config.getAssetsToTrack());
    }

    @Override
    public List<SpotIncomeState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<SpotIncomeState> spotIncomeStates = new ArrayList<>();
        for (int i = 0; i < events.size() - 1; i++) {
            final AbstractEvent currentEvent = events.get(i);
            final AbstractEvent nextEvent = events.get(i + 1);

            if ((currentEvent.getEventType() != EventType.ORDER_TRADE_UPDATE
                    && currentEvent.getEventType() != EventType.BALANCE_UPDATE)
                    || nextEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) {
                continue;
            }

            if (ChronoUnit.SECONDS.between(currentEvent.getDateTime(),
                    nextEvent.getDateTime()) > MAX_SECONDS_DELAY_FOR_VALID_EVENTS) {
                continue;
            }

            final SpotIncomeState incomeState = spotIncomeStates.size() == 0 ?
                    new SpotIncomeState(currentEvent.getDateTime())
                    : new SpotIncomeState(currentEvent.getDateTime(), spotIncomeStates.get(spotIncomeStates.size() - 1));

            
            // TODO FOR NOW VERY QUESTIONABLE HOW TO HANDLE
         	if (currentEvent.getEventType() == EventType.BALANCE_UPDATE) {
         		final BalanceUpdateEvent balanceEvent = (BalanceUpdateEvent) currentEvent;
         		if (!assetsToTrack.contains(balanceEvent.getBalances()) && assetsToTrack.size() != 0) {
         			continue;
         		}
         		incomeState.processOrderDetails(balanceEvent.getBalances(), balanceEvent.getBalanceDelta(), null);
         		spotIncomeStates.add(incomeState);
         		continue;
         	}

            final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) currentEvent;
            final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;
            final String orderSymbol = orderEvent.getSymbol().replace(USDT, "");

            if (!orderEvent.getOrderStatus().equals("FILLED")
                    || (!assetsToTrack.contains(orderSymbol) && assetsToTrack.size() != 0)) {
                continue;
            }

            logTrade(orderEvent);
            
            //update asset balances
			final List<Asset> updatedAssets = accEvent.getBalances().stream()
					.filter(asset -> assetsToTrack.contains(asset.getAsset()) || assetsToTrack.size() == 0)
					.map(asset -> new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked())))
					.collect(Collectors.toList());
			incomeState.updateAssetBalance(updatedAssets);
			//track user's absolute trade performance
            if (orderEvent.getSide().equals("BUY")) {
                incomeState.processOrderDetails(orderSymbol, orderEvent.getOriginalQuantity(), includeCommissionToBuyPrice(orderEvent));
            } else if (orderEvent.getSide().equals("SELL")) {
                incomeState.processOrderDetails(orderSymbol, orderEvent.getOriginalQuantity().negate(), includeCommissionToSellPrice(orderEvent));
            } else {
                throw new IllegalArgumentException("Unrecognized order.Side");
            }

            spotIncomeStates.add(incomeState);
        }
        return spotIncomeStates;
    }

    private BigDecimal includeCommissionToBuyPrice(OrderTradeUpdateEvent orderEvent) {
		// COMISSION WORKS ONLY FOR USDT NOW
		return orderEvent.getCommissionAsset().equals(USDT)
				? orderEvent.getPrice().add(
						orderEvent.getCommission().divide(orderEvent.getOriginalQuantity(), Constants.MATH_CONTEXT))
				: orderEvent.getPrice();
	}

	private BigDecimal includeCommissionToSellPrice(OrderTradeUpdateEvent orderEvent) {
		// COMISSION WORKS ONLY FOR USDT NOW
		return orderEvent.getCommissionAsset().equals(USDT)
				? orderEvent.getPrice().subtract(
						orderEvent.getCommission().divide(orderEvent.getOriginalQuantity(), Constants.MATH_CONTEXT))
				: orderEvent.getPrice();
	}

    private void logTrade(OrderTradeUpdateEvent orderEvent) {
        final BigDecimal quoteAssetQty = orderEvent.getOriginalQuantity()
                .multiply(orderEvent.getPriceOfLastFilledTrade());
        final String str = String.format("%s %s %s %s for total of %s quoteAsset",
                orderEvent.getDateTime().format(ISO_DATE_TIME), orderEvent.getSide(),
                orderEvent.getOriginalQuantity().toPlainString(), orderEvent.getSymbol(),
                quoteAssetQty.toPlainString());
        System.out.println(str);
    }
}