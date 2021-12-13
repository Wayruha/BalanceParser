package com.example.binanceparser.algorithm;

import static com.example.binanceparser.Constants.AXS;
import static com.example.binanceparser.Constants.BTC;
import static com.example.binanceparser.Constants.BUSD;
import static com.example.binanceparser.Constants.ETH;
import static com.example.binanceparser.Constants.USDT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;

public class SpotBalanceCalcAlgorithmTest {

	private List<AbstractEvent> aelist = null;
	private List<EventBalanceState> noAssetsBSlist = null;
	private List<EventBalanceState> bslist = null;
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
		config.setAssetsToTrack(List.of(USDT, BUSD, BTC, ETH, AXS));
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
				.symbol(USDT)
				.orderStatus("FILLED").build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE).build());
		aelist.add(FuturesAccountUpdateEvent.builder().eventType(EventType.FUTURES_ACCOUNT_UPDATE).build());//should skip
		aelist.add(BalanceUpdateEvent.builder()
				.eventType(EventType.BALANCE_UPDATE).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE).build());
		aelist.add(OrderTradeUpdateEvent.builder()
				.eventType(EventType.ORDER_TRADE_UPDATE).build());
		aelist.add(AccountPositionUpdateEvent.builder()
				.eventType(EventType.ACCOUNT_POSITION_UPDATE).build());
		
		//definind balance states for config with no assets to track(track all assets available)
		noAssetsBSlist.add(new EventBalanceState());
		noAssetsBSlist.add(new EventBalanceState());
		noAssetsBSlist.add(new EventBalanceState());
		
		//defining balance states for config with specified assets to track
		bslist.add(new EventBalanceState());
		bslist.add(new EventBalanceState());
		bslist.add(new EventBalanceState());
		
	}
	
	@Test
	public void shouldReturnCorrectBalanceStatesForAllAssets() {		
		try {
			SpotBalanceCalcAlgorithm alg = new SpotBalanceCalcAlgorithm(noAssetsConfig);
			List<EventBalanceState> acceptedBSlist = alg.processEvents(aelist);
			assertEquals(noAssetsBSlist, acceptedBSlist);
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldReturnCorrectBalanceStatesForSpecifiedAssets() {
		try {
			SpotBalanceCalcAlgorithm alg = new SpotBalanceCalcAlgorithm(config);
			List<EventBalanceState> acceptedBSlist = alg.processEvents(aelist);
			assertEquals(bslist, acceptedBSlist);
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
}
