package com.example.binanceparser.algorithm;

import static com.example.binanceparser.Constants.USDT;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;

public class TestSpotBalancecalcAlgorithm implements CalculationAlgorithm<SpotIncomeState> {

	private final BalanceVisualizerConfig config;
	private final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;
	
	public TestSpotBalancecalcAlgorithm(BalanceVisualizerConfig config) {
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

			//FOR NOW VERY QUESTIONABLE HOW TO HANDLE
			if (currentEvent.getEventType() == EventType.BALANCE_UPDATE) {
				final BalanceUpdateEvent balanceUpdateEvent = (BalanceUpdateEvent) currentEvent;
				SpotIncomeState incomeState = new SpotIncomeState(balanceUpdateEvent.getDateTime());
				incomeState.findAssetState(balanceUpdateEvent.getBalances())
					.updateAssetState(balanceUpdateEvent.getBalanceDelta(), null);
				//spotIncomeStates.add(incomeState);
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
			
			SpotIncomeState incomeState = new SpotIncomeState(currentEvent.getDateTime());
			
			if (incomeState.findAssetState(orderSymbol) == null) {
				incomeState.setAssetState(incomeState.new AssetState(
						orderSymbol, 
						BigDecimal.ZERO,
						BigDecimal.ZERO)
						);
			}
			
			if(orderEvent.getSide().equals("BUY")) {
				incomeState.findAssetState(orderSymbol)
					.updateAssetState(
						orderEvent.getOriginalQuantity(), 
						includeCommissionToBuyPrice(orderEvent)
						);
			}
			else if(orderEvent.getSide().equals("SELL")) {
				incomeState.findAssetState(orderSymbol)
					.updateAssetState(
						orderEvent.getOriginalQuantity().negate(), 
						includeCommissionToSellPrice(orderEvent)
						);
			}
			else {
				continue;
			}
			
			spotIncomeStates.add(incomeState);	
		}
		return spotIncomeStates;
	}
	
	private BigDecimal includeCommissionToBuyPrice(OrderTradeUpdateEvent orderEvent) {
		return orderEvent.getPrice()//COMISSION WORKS ONLY FOR USDT NOW
				.add(orderEvent.getCommission().divide(orderEvent.getOriginalQuantity(), 8, RoundingMode.FLOOR));
	}
	
	private BigDecimal includeCommissionToSellPrice(OrderTradeUpdateEvent orderEvent) {
		return orderEvent.getPrice()//COMISSION WORKS ONLY FOR USDT NOW
				.subtract(orderEvent.getCommission().divide(orderEvent.getOriginalQuantity(), 8, RoundingMode.FLOOR));
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
