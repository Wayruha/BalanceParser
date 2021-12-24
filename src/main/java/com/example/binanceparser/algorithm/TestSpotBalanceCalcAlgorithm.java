package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.AssetInfo;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

			final SpotIncomeState incomeState = spotIncomeStates.size() == 0
					? new SpotIncomeState(currentEvent.getDateTime())
					: new SpotIncomeState(currentEvent.getDateTime(),
							spotIncomeStates.get(spotIncomeStates.size() - 1));

			if (currentEvent.getEventType() == EventType.BALANCE_UPDATE) {
				final BalanceUpdateEvent balanceEvent = (BalanceUpdateEvent) currentEvent;
				final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

				if (!assetsToTrack.contains(balanceEvent.getBalances()) && assetsToTrack.size() != 0) {
					continue;
				}
				// update asset balances
				incomeState.updateAssetBalance(getAssets(accEvent.getBalances(),
						new AssetInfo(balanceEvent.getDateTime(), "", BigDecimal.ZERO), assetsToTrack));
				incomeState.processOrderDetails(balanceEvent.getBalances(), balanceEvent.getBalanceDelta(), null);
				spotIncomeStates.add(incomeState);
				continue;
			}

			final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) currentEvent;
			final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

			if (!orderEvent.getOrderStatus().equals("FILLED")
					|| (!assetsToTrack.contains(orderEvent.getOrderSymbol()) && assetsToTrack.size() != 0)) {
				continue;
			}

			logTrade(orderEvent);

			incomeState.updateAssetBalance(getAssets(accEvent.getBalances(),
					new AssetInfo(orderEvent.getDateTime(), orderEvent.getQuoteAsset(), orderEvent.getPrice()), assetsToTrack));
			incomeState.processOrderDetails(orderEvent.getOrderSymbol(), orderEvent.getTradeDelta(),
					orderEvent.getPriceIncludingCommission());

			spotIncomeStates.add(incomeState);
		}
		return spotIncomeStates;
	}

	private List<Asset> getAssets(List<AccountPositionUpdateEvent.Asset> balances, AssetInfo assetInfo,
			List<String> assetsToTrack) {
		return balances.stream().filter(asset -> assetsToTrack.contains(asset.getAsset()) || assetsToTrack.size() == 0)
				.map(asset -> new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()), assetInfo))
				.collect(Collectors.toList());
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