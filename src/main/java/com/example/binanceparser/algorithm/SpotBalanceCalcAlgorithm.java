package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.valueOf;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class SpotBalanceCalcAlgorithm implements CalculationAlgorithm {
	private final BalanceVisualizerConfig config;
	final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;
	private final HashMap<String, BigDecimal> assetRate;

	public SpotBalanceCalcAlgorithm(BalanceVisualizerConfig config) {
		this.config = config;
		this.assetRate = new HashMap<>();
		assetRate.put(USDT, valueOf(1.0)); // because order event has no USDT rate
		assetRate.put(BUSD, valueOf(1.0)); // because order event has no BUSD rate
	}

	@Override
	public List<EventBalanceState> processEvents(List<AbstractEvent> events) {
		return processEvents(events, config.getAssetsToTrack());
	}

	@Override
	public List<EventBalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {//for now checks if assetsToTrack is empty=>process all assets available
		final List<EventBalanceState> eventBalanceStates = new ArrayList<>();
		Map<String, Asset> actualBalance = new HashMap<>();
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

			if (currentEvent.getEventType() == EventType.BALANCE_UPDATE) {
				final BalanceUpdateEvent balanceUpdateEvent = (BalanceUpdateEvent) currentEvent;
				eventBalanceStates.add(processBalanceUpdate(nextEvent, actualBalance,
						balanceUpdateEvent.getBalanceDelta(), assetsToTrack));
				continue;
			}

			final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) currentEvent;
			final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;
			final String orderSymbol = orderEvent.getSymbol().replace(USDT, "");

			if (!orderEvent.getOrderStatus().equals("FILLED")
					|| (!assetsToTrack.contains(orderSymbol) && assetsToTrack.size() != 0)) {
				continue;
			}

			Set<Asset> newEventAssets = accEvent.getBalances().stream()
					.filter(asset -> assetsToTrack.contains(asset.getAsset()) || assetsToTrack.size() == 0)
					.map(asset -> new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked())))
					.collect(Collectors.toSet());

			logTrade(orderEvent);

			if (orderEvent.getSide().equals("BUY") && assetRate.containsKey(orderSymbol)) {
				BigDecimal newQuantity = valueOf(orderEvent.getOriginalQuantity());
				BigDecimal currentQuantity = actualBalance.get(orderSymbol).getAvailableBalance();
				BigDecimal newPrice = orderEvent.getPriceOfLastFilledTrade();
				BigDecimal currentPrice = assetRate.get(orderSymbol);

				final BigDecimal quoteAssetQty = newQuantity.multiply(newPrice);
				final BigDecimal existingQuoteQty = currentQuantity.multiply(currentPrice);

				BigDecimal newTotalAssetQty = quoteAssetQty.add(existingQuoteQty);
				assetRate.put(orderSymbol,
						newTotalAssetQty.divide(newQuantity.add(currentQuantity), 2, RoundingMode.FLOOR));
			} else {
				assetRate.put(orderSymbol, orderEvent.getPriceOfLastFilledTrade());
			}

			actualBalance = processBalance(actualBalance, newEventAssets);
			eventBalanceStates
					.add(new EventBalanceState(accEvent.getDateTime(), new HashSet<>(actualBalance.values()), null));
		}
		return config.isConvertToUSD() ? balanceToUSDT(eventBalanceStates) : eventBalanceStates;
	}

	private void logTrade(OrderTradeUpdateEvent orderEvent) {
		final BigDecimal quoteAssetQty = valueOf(orderEvent.getOriginalQuantity())
				.multiply(orderEvent.getPriceOfLastFilledTrade());
		final String str = String.format("%s %s %s %s for total of %s quoteAsset",
				orderEvent.getDateTime().format(ISO_DATE_TIME), orderEvent.getSide(),
				valueOf(orderEvent.getOriginalQuantity()).toPlainString(), orderEvent.getSymbol(),
				quoteAssetQty.toPlainString());
		System.out.println(str);
	}

	public EventBalanceState processBalanceUpdate(AbstractEvent nextEvent, Map<String, Asset> actualBalance,
			BigDecimal balanceUpdateDelta, List<String> assetsToTrack) {
		final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;
		Set<Asset> newEventAssets = accEvent.getBalances().stream()
				.filter(asset -> assetsToTrack.contains(asset.getAsset()) || assetsToTrack.size() == 0)
				.map(asset -> new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked())))
				.collect(Collectors.toSet());

		actualBalance = processBalance(actualBalance, newEventAssets);
		return new EventBalanceState(accEvent.getDateTime(), new HashSet<>(actualBalance.values()), balanceUpdateDelta);
	}

	public Map<String, Asset> processBalance(Map<String, Asset> actualBalance, Set<Asset> newBalance) {
		newBalance.forEach(asset -> actualBalance.put(asset.getAsset(), asset));
		return actualBalance;
	}

	public List<EventBalanceState> balanceToUSDT(List<EventBalanceState> eventBalanceStates) {
		List<EventBalanceState> updatedEventBalanceState = new ArrayList<>();
		for (EventBalanceState state : eventBalanceStates) {
			Set<Asset> assets = new HashSet<>();
			BigDecimal balance = new BigDecimal(0);
			for (Asset asset : state.getAssets()) {
				if (assetRate.get(asset.getAsset()) == null) {// should check if this condition is necessary
					balance = balance.add(asset.getAvailableBalance());
				} else {
					balance = balance.add(asset.getAvailableBalance().multiply(assetRate.get(asset.getAsset())));
				}
			}
			assets.add(new Asset(USD, balance));
			updatedEventBalanceState
					.add(new EventBalanceState(state.getDateTime(), assets, state.getBalanceUpdateDelta()));
		}
		// System.out.println(updatedEventBalanceState);
		return updatedEventBalanceState;
	}

}
