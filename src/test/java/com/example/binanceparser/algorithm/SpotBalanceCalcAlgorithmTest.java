package com.example.binanceparser.algorithm;

import static com.example.binanceparser.Constants.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.AssetMetadata;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;

public class SpotBalanceCalcAlgorithmTest {
	private static List<AbstractEvent> aelist = new ArrayList<>();
	private static List<SpotIncomeState> bsList = new ArrayList<>();
	private static BalanceVisualizerConfig config = new BalanceVisualizerConfig();

	@BeforeAll
	public static void init() {
		// defining config objects
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse("2021-08-16 00:01:00", dateFormat);
		config.setStartTrackDate(LocalDateTime.parse("2021-08-16 00:00:00", dateFormat));
		config.setFinishTrackDate(LocalDateTime.parse("2021-09-15 00:00:00", dateFormat));
		config.setInputFilepath("C:/Users/Sanya/Desktop/ParserOutput/logs");
		config.setOutputDir("C:/Users/Sanya/Desktop/ParserOutput");
		config.setAssetsToTrack(Collections.emptyList());
		config.setConvertToUSD(true);

		// defining events and income states
		BalanceUpdateEvent balanceEvent;
		OrderTradeUpdateEvent orderEvent;
		AccountPositionUpdateEvent accEvent;
		SpotIncomeState incomeState;
		AssetMetadata assetMetadata;

		aelist.add(OrderTradeUpdateEvent.builder().eventType(EventType.ORDER_TRADE_UPDATE).dateTime(dateTime)
				.orderStatus("NEW").build());// should skip
		aelist.add(AccountPositionUpdateEvent.builder().eventType(EventType.ACCOUNT_POSITION_UPDATE).dateTime(dateTime)
				.build());// should skip
		aelist.add(FuturesOrderTradeUpdateEvent.builder().eventType(EventType.FUTURES_ORDER_TRADE_UPDATE)
				.dateTime(dateTime).build());// should skip
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).dateTime(dateTime)
				.build());// should skip
		// buying 0.001 BTC with price 45000 UST
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("BUY").price(new BigDecimal("45000"))
				.priceOfLastFilledTrade(new BigDecimal("45000")).originalQuantity(new BigDecimal("0.001"))
				.commission(new BigDecimal("0.5")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.0015"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("100"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime);
		assetMetadata = AssetMetadata.builder().dateOfLastTransaction(orderEvent.getDateTime())
				.quoteAsset(orderEvent.getQuoteAsset()).priceOfLastTrade(orderEvent.getPriceOfLastFilledTrade())
				.build();
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, assetMetadata));
		incomeState.processOrderDetails(orderEvent.getBaseAsset(), orderEvent.getTradeDelta(),
				orderEvent.getPriceIncludingCommission());
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).dateTime(dateTime)
				.build());// should skip
		// withdrawing 10 USDT
		balanceEvent = BalanceUpdateEvent.builder().dateTime(dateTime).eventType(EventType.BALANCE_UPDATE)
				.balances(USDT).balanceDelta(new BigDecimal("-10")).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(
						List.of(new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("90"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		assetMetadata = AssetMetadata.builder().dateOfLastTransaction(balanceEvent.getDateTime()).quoteAsset("")
				.priceOfLastTrade(new BigDecimal("0")).build();
		incomeState.updateAssetBalance(extractAssetsFromEvent(balanceEvent.getBalances(), accEvent, assetMetadata));
		incomeState.processOrderDetails(balanceEvent.getBalances(), balanceEvent.getBalanceDelta(), null);
		bsList.add(incomeState);
		aelist.add(balanceEvent);
		aelist.add(accEvent);
		// selling 0.05 ETH with price 4500
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH + USDT).orderStatus("FILLED").side("SELL").price(new BigDecimal("4500"))
				.priceOfLastFilledTrade(new BigDecimal("4500")).originalQuantity(new BigDecimal("0.05"))
				.commission(new BigDecimal("0.5")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(new AccountPositionUpdateEvent.Asset(ETH, new BigDecimal("0.1"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("314.5"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		assetMetadata = AssetMetadata.builder().dateOfLastTransaction(orderEvent.getDateTime())
				.quoteAsset(orderEvent.getQuoteAsset()).priceOfLastTrade(orderEvent.getPriceOfLastFilledTrade())
				.build();
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, assetMetadata));
		incomeState.processOrderDetails(orderEvent.getBaseAsset(), orderEvent.getTradeDelta(),
				orderEvent.getPriceIncludingCommission());
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		// buying 0.001 BTC with price 50000
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("BUY").price(new BigDecimal("50000"))
				.priceOfLastFilledTrade(new BigDecimal("50000")).originalQuantity(new BigDecimal("0.001"))
				.commission(new BigDecimal("1")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.0025"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("263.5"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		assetMetadata = AssetMetadata.builder().dateOfLastTransaction(orderEvent.getDateTime())
				.quoteAsset(orderEvent.getQuoteAsset()).priceOfLastTrade(orderEvent.getPriceOfLastFilledTrade())
				.build();
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, assetMetadata));
		incomeState.processOrderDetails(orderEvent.getBaseAsset(), orderEvent.getTradeDelta(),
				orderEvent.getPriceIncludingCommission());
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		// selling 0.002 BTC with price 51000
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("SELL").price(new BigDecimal("51000"))
				.priceOfLastFilledTrade(new BigDecimal("51000")).originalQuantity(new BigDecimal("0.002"))
				.commission(new BigDecimal("1.5")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.0005"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("364"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		assetMetadata = AssetMetadata.builder().dateOfLastTransaction(orderEvent.getDateTime())
				.quoteAsset(orderEvent.getQuoteAsset()).priceOfLastTrade(orderEvent.getPriceOfLastFilledTrade())
				.build();
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, assetMetadata));
		incomeState.processOrderDetails(orderEvent.getBaseAsset(), orderEvent.getTradeDelta(),
				orderEvent.getPriceIncludingCommission());
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
	}

	private static List<Asset> extractAssetsFromEvent(String baseAsset, AccountPositionUpdateEvent event,
			AssetMetadata assetMetadata) {
		return event.getBalances().stream().map(asset -> new Asset(asset.getAsset(),
				asset.getFree().add(asset.getLocked()), asset.getAsset().equals(baseAsset) ? assetMetadata : null))
				.collect(Collectors.toList());
	}

	@Test
	public void shouldReturnCorrectBalanceStatesForAllAssets() throws SecurityException, IllegalArgumentException {
		TestSpotBalanceCalcAlgorithm alg = new TestSpotBalanceCalcAlgorithm(config);
		List<SpotIncomeState> acceptedBSlist = alg.processEvents(aelist);
		assertIterableEquals(bsList, acceptedBSlist);
	}
}