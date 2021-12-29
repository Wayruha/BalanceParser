package com.example.binanceparser.algorithm;

import static com.example.binanceparser.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.annotation.BeforeTestClass;
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
	
	@BeforeTestClass
	public static void init() {
		//defining config objects
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		config.setStartTrackDate(LocalDateTime.parse("2021-08-16 00:00:00", dateFormat));
		config.setFinishTrackDate(LocalDateTime.parse("2021-09-15 00:00:00", dateFormat));
		config.setInputFilepath("C:/Users/Sanya/Desktop/ParserOutput/logs");
		config.setOutputDir("C:/Users/Sanya/Desktop/ParserOutput");
		config.setAssetsToTrack(Collections.emptyList());
		config.setConvertToUSD(true);
		
		//defining events and income states
		BalanceUpdateEvent balanceEvent;
		OrderTradeUpdateEvent orderEvent;
		AccountPositionUpdateEvent accEvent;
		SpotIncomeState incomeState;
		
		aelist.add(OrderTradeUpdateEvent.builder().eventType(EventType.ORDER_TRADE_UPDATE).orderStatus("NEW").build());//should skip
		aelist.add(FuturesOrderTradeUpdateEvent.builder().eventType(EventType.FUTURES_ORDER_TRADE_UPDATE).build());//should skip
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).build());//should skip
		//buying 0.001 BTC with price 45000 UST
		orderEvent = OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC+USDT)
				.orderStatus("FILLED")
				.side("BUY")
				.price(BigDecimal.valueOf(45000.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(45000.0))
				.originalQuantity(BigDecimal.valueOf(0.001))
				.commission(BigDecimal.valueOf(0.5))
				.commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, BigDecimal.valueOf(0.0015), BigDecimal.valueOf(0.0)),
						new AccountPositionUpdateEvent.Asset(USDT, BigDecimal.valueOf(100), BigDecimal.valueOf(0.0))
						)).build();
		incomeState = new SpotIncomeState(null);
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, null));
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).build());//should skip
		//withdrawing 10 USDT
		balanceEvent = BalanceUpdateEvent.builder()
				.eventType(EventType.BALANCE_UPDATE)
				.balances(USDT)
				.balanceDelta(BigDecimal.valueOf(-10.0)).build();
		accEvent = AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(USDT, BigDecimal.valueOf(90), BigDecimal.valueOf(0.0))
						)).build();
		incomeState = new SpotIncomeState(null, incomeState);
		incomeState.updateAssetBalance(extractAssetsFromEvent(balanceEvent.getBalances(), accEvent, null));
		bsList.add(incomeState);
		aelist.add(balanceEvent);
		aelist.add(accEvent);
		//selling 0.05 ETH with price 4500
		orderEvent = OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH+USDT)
				.orderStatus("FILLED")
				.side("SELL")
				.price(BigDecimal.valueOf(4500.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(4500.0))
				.originalQuantity(BigDecimal.valueOf(0.05))
				.commission(BigDecimal.valueOf(0.5))
				.commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(ETH, BigDecimal.valueOf(0.10), BigDecimal.valueOf(0.0)),
						new AccountPositionUpdateEvent.Asset(USDT, BigDecimal.valueOf(314.5), BigDecimal.valueOf(0.0))
						)).build();
		incomeState = new SpotIncomeState(null, incomeState);
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, null));
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		//buying 0.001 BTC with price 50000
		orderEvent = OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC+USDT)
				.orderStatus("FILLED")
				.side("BUY")
				.price(BigDecimal.valueOf(50000.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(50000.0))
				.originalQuantity(BigDecimal.valueOf(0.001))
				.commission(BigDecimal.valueOf(1.0))
				.commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, BigDecimal.valueOf(0.0025), BigDecimal.valueOf(0.0)),
						new AccountPositionUpdateEvent.Asset(USDT, BigDecimal.valueOf(263.5), BigDecimal.valueOf(0.0))
						)).build();
		incomeState = new SpotIncomeState(null, incomeState);
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, null));
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		//selling 0.002 BTC with price 51000
		orderEvent = OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC+USDT)
				.orderStatus("FILLED")
				.side("SELL")
				.price(BigDecimal.valueOf(51000.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(51000.0))
				.originalQuantity(BigDecimal.valueOf(0.002))
				.commission(BigDecimal.valueOf(1.5))
				.commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, BigDecimal.valueOf(0.0005), BigDecimal.valueOf(0.0)),
						new AccountPositionUpdateEvent.Asset(USDT, BigDecimal.valueOf(364), BigDecimal.valueOf(0.0))
						)).build();
		incomeState = new SpotIncomeState(null, incomeState);
		incomeState.updateAssetBalance(extractAssetsFromEvent(orderEvent.getBaseAsset(), accEvent, null));
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
	}
	
	private static List<Asset> extractAssetsFromEvent(String baseAsset, AccountPositionUpdateEvent event,
			AssetMetadata assetMetadata) {
		return event.getBalances().stream().filter((asset) -> asset.getAsset().equals(baseAsset))
				.map(asset -> new Asset(asset.getAsset(), asset.getFree().add(asset.getLocked()), assetMetadata))
				.collect(Collectors.toList());
	}
	
	@Test
	public void shouldReturnCorrectBalanceStatesForAllAssets() throws SecurityException, IllegalArgumentException{		
		TestSpotBalanceCalcAlgorithm alg = new TestSpotBalanceCalcAlgorithm(config);
		List<SpotIncomeState> acceptedBSlist = alg.processEvents(aelist);
		assertEquals(bsList, acceptedBSlist);
	}
}