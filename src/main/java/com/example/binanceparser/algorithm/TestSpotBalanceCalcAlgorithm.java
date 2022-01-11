package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.AssetMetadata;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.TransactionType;
import com.example.binanceparser.domain.events.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.EXCHANGE_INFO;
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

				logBalanceUpdate(balanceEvent);
				// update asset balances
				final AssetMetadata assetMetadata = AssetMetadata.builder()
						.dateOfLastTransaction(balanceEvent.getDateTime()).quoteAsset("")
						.priceOfLastTrade(BigDecimal.ZERO).build();
				final List<Asset> assetsInvolved = extractAssetsFromEvent(balanceEvent.getBalances(), accEvent, assetMetadata);
				incomeState.updateAssetBalance(assetsInvolved);
				incomeState.processOrderDetails(balanceEvent.getBalances(), balanceEvent.getBalanceDelta(), null);
				spotIncomeStates.add(incomeState);
				continue;
			}

			final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) currentEvent;
			final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

			if (!orderEvent.getOrderStatus().equals("FILLED")) {
				continue;
			}

			logTrade(orderEvent, incomeState);

			final AssetMetadata assetMetadata = AssetMetadata.builder().dateOfLastTransaction(orderEvent.getDateTime())
					.quoteAsset(orderEvent.getQuoteAsset()).priceOfLastTrade(orderEvent.getPriceOfLastFilledTrade())
					.build();

			final List<Asset> assetsInvolved = extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent,
					assetMetadata);
			incomeState.updateAssetBalance(assetsInvolved);
			incomeState.processOrderDetails(orderEvent.getBaseAsset(), orderEvent.getTradeDelta(),
					orderEvent.getPriceIncludingCommission());

			spotIncomeStates.add(incomeState);
		}
		return spotIncomeStates;
	}

	private List<Asset> extractAssetsFromEvent(String baseAsset, AccountPositionUpdateEvent event,
			AssetMetadata assetMetadata) {
		return event.getBalances().stream().map(asset ->
			Asset.builder()
					.asset(asset.getAsset())
					.balance(asset.getFree().add(asset.getLocked()))
					.assetMetadata(asset.getAsset().equals(baseAsset) ? assetMetadata : null)
					.build()
		).collect(Collectors.toList());
	}

	private void logBalanceUpdate(BalanceUpdateEvent balanceEvent) {
		TransactionType transactionType = balanceEvent.getBalanceDelta().compareTo(BigDecimal.ZERO) > 0
				? TransactionType.DEPOSIT : TransactionType.WITHDRAW;
		final String str = String.format("%s %s %s %s", balanceEvent.getDateTime().format(ISO_DATE_TIME),
				transactionType, balanceEvent.getBalances(), balanceEvent.getBalanceDelta().abs());
		System.out.println(str);
	}

	private void logTrade(OrderTradeUpdateEvent orderEvent, SpotIncomeState prevState) {
		final BigDecimal quoteAssetQty = orderEvent.getOriginalQuantity()
				.multiply(orderEvent.getPriceOfLastFilledTrade());
		String str = String.format("%s %s %s %s for total of %s quoteAsset",
				orderEvent.getDateTime().format(ISO_DATE_TIME), orderEvent.getSide(),
				orderEvent.getOriginalQuantity().toPlainString(), orderEvent.getSymbol(),
				quoteAssetQty.toPlainString());

		if(orderEvent.getSide().equals("SELL")){
			final String baseAsset = EXCHANGE_INFO.getSymbolInfo(orderEvent.getSymbol()).getBaseAsset();
			final SpotIncomeState.LockedAsset lockedState = prevState.findLockedAsset(baseAsset);
			if(lockedState != null){
				str += ". Profit:" + quoteAssetQty.subtract(lockedState.totalQuoteAssetValue()).toPlainString();
			}
		}
		System.out.println(str);
	}
}