package com.example.binanceparser.algorithm;

import static com.example.binanceparser.Constants.AXS;
import static com.example.binanceparser.Constants.BTC;
import static com.example.binanceparser.Constants.BUSD;
import static com.example.binanceparser.Constants.ETH;
import static com.example.binanceparser.Constants.USDT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent.Asset;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;

public class SpotBalanceCalcAlgorithmTest {

	private List<AbstractEvent> aelist = null;
	private List<SpotIncomeState> noAssetsBSlist = null;
	private List<SpotIncomeState> bslist = null;
	private BalanceVisualizerConfig noAssetsConfig = null;
	private BalanceVisualizerConfig config = null;
	
	@BeforeAll
	public void init() {
		
		//defining config objects
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		config.setStartTrackDate(LocalDateTime.parse("2021-08-16 00:00:00", dateFormat));
		config.setFinishTrackDate(LocalDateTime.parse("2021-09-15 00:00:00", dateFormat));
		config.setInputFilepath("C:/Users/Sanya/Desktop/ParserOutput/logs");
		config.setOutputDir("C:/Users/Sanya/Desktop/ParserOutput");
		config.setAssetsToTrack(List.of(BUSD, BTC));
		config.setConvertToUSD(true);
		
		config.setStartTrackDate(LocalDateTime.parse("2021-08-16 00:00:00", dateFormat));
		config.setFinishTrackDate(LocalDateTime.parse("2021-09-15 00:00:00", dateFormat));
		config.setInputFilepath("C:/Users/Sanya/Desktop/ParserOutput/logs");
		config.setOutputDir("C:/Users/Sanya/Desktop/ParserOutput");
		config.setAssetsToTrack(Collections.emptyList());
		config.setConvertToUSD(true);
		
		//defining events
		aelist.add(OrderTradeUpdateEvent.builder().eventType(EventType.ORDER_TRADE_UPDATE).orderStatus("NEW").build());//should skip
		aelist.add(FuturesOrderTradeUpdateEvent.builder().eventType(EventType.FUTURES_ORDER_TRADE_UPDATE).build());//should skip
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).build());//should skip
		aelist.add(OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC+USDT)
				.orderStatus("FILLED")
				.side("BUY")
				.price(BigDecimal.valueOf(45000.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(45000.0))
				.originalQuantity(BigDecimal.valueOf(0.001))
				.commission(BigDecimal.valueOf(0.5))
				.commissionAsset(USDT).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new Asset(BTC, BigDecimal.valueOf(0.0015), BigDecimal.valueOf(0.0)),
						new Asset(ETH, BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.0)),
						new Asset(USDT, BigDecimal.valueOf(100), BigDecimal.valueOf(0.0)),
						new Asset(AXS, BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.0))
						)).build());
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).build());//should skip
		aelist.add(BalanceUpdateEvent.builder()
				.eventType(EventType.BALANCE_UPDATE)
				.balances(USDT)
				.balanceDelta(BigDecimal.valueOf(-10.0)).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new Asset(BTC, BigDecimal.valueOf(0.0015), BigDecimal.valueOf(0.0)),
						new Asset(ETH, BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.0)),
						new Asset(USDT, BigDecimal.valueOf(90), BigDecimal.valueOf(0.0)),
						new Asset(AXS, BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.0))
						)).build());
		aelist.add(OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH+USDT)
				.orderStatus("FILLED")
				.side("SELL")
				.price(BigDecimal.valueOf(4500.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(4500.0))
				.originalQuantity(BigDecimal.valueOf(0.05))
				.commission(BigDecimal.valueOf(0.5))
				.commissionAsset(USDT).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new Asset(BTC, BigDecimal.valueOf(0.0015), BigDecimal.valueOf(0.0)),
						new Asset(ETH, BigDecimal.valueOf(0.10), BigDecimal.valueOf(0.0)),
						new Asset(USDT, BigDecimal.valueOf(314.5), BigDecimal.valueOf(0.0)),
						new Asset(AXS, BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.0))
						)).build());
		aelist.add(OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC+USDT)
				.orderStatus("FILLED")
				.side("BUY")
				.price(BigDecimal.valueOf(50000.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(50000.0))
				.originalQuantity(BigDecimal.valueOf(0.001))
				.commission(BigDecimal.valueOf(1.0))
				.commissionAsset(USDT).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new Asset(BTC, BigDecimal.valueOf(0.0025), BigDecimal.valueOf(0.0)),
						new Asset(ETH, BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.0)),
						new Asset(USDT, BigDecimal.valueOf(263.5), BigDecimal.valueOf(0.0)),
						new Asset(AXS, BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.0))
						)).build());
		aelist.add(OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC+USDT)
				.orderStatus("FILLED")
				.side("SELL")
				.price(BigDecimal.valueOf(51000.0))
				.priceOfLastFilledTrade(BigDecimal.valueOf(51000.0))
				.originalQuantity(BigDecimal.valueOf(0.002))
				.commission(BigDecimal.valueOf(1.5))
				.commissionAsset(USDT).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new Asset(BTC, BigDecimal.valueOf(0.0005), BigDecimal.valueOf(0.0)),
						new Asset(ETH, BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.0)),
						new Asset(USDT, BigDecimal.valueOf(364), BigDecimal.valueOf(0.0)),
						new Asset(AXS, BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.0))
						)).build());
		
		
		//definind income states for config with no assets to track(track all assets available)
		noAssetsBSlist.add(new SpotIncomeState(BigDecimal.ZERO, null));
		noAssetsBSlist.add(new SpotIncomeState(BigDecimal.valueOf(-10), null));
		noAssetsBSlist.add(new SpotIncomeState(BigDecimal.valueOf(-10), null));
		noAssetsBSlist.add(new SpotIncomeState(BigDecimal.valueOf(-10), null));
		noAssetsBSlist.add(new SpotIncomeState(BigDecimal.valueOf(-6), null));
		
		//defining income states for config with specified assets to track
		bslist.add(new SpotIncomeState(BigDecimal.ZERO, null));
		bslist.add(new SpotIncomeState(BigDecimal.ZERO, null));
		bslist.add(new SpotIncomeState(BigDecimal.valueOf(4), null));
		
	}
	
	@Test
	public void shouldReturnCorrectBalanceStatesForAllAssets() throws SecurityException, IllegalArgumentException{		
		TestSpotBalancecalcAlgorithm alg = new TestSpotBalancecalcAlgorithm(noAssetsConfig);
		List<SpotIncomeState> acceptedBSlist = alg.processEvents(aelist);
		assertEquals(noAssetsBSlist, acceptedBSlist);
	}
	
	@Test
	public void shouldReturnCorrectBalanceStatesForSpecifiedAssets() throws SecurityException, IllegalArgumentException {
		TestSpotBalancecalcAlgorithm alg = new TestSpotBalancecalcAlgorithm(config);
		List<SpotIncomeState> acceptedBSlist = alg.processEvents(aelist);
		assertEquals(bslist, acceptedBSlist);
	}
	
}
