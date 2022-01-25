package com.example.binanceparser.algorithm;

import static com.example.binanceparser.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.LockedAsset;
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
	private static SpotBalanceCalcAlgorithm calcAlgorithm = new SpotBalanceCalcAlgorithm();

	@BeforeAll
	public static void init() {
		// defining config objects
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse("2021-08-16 00:01:00", dateFormat);

		// defining events and income states
		BalanceUpdateEvent balanceEvent;
		OrderTradeUpdateEvent orderEvent;
		AccountPositionUpdateEvent accEvent;
		SpotIncomeState incomeState;

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
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
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
		calcAlgorithm.processBalanceUpdate(incomeState, balanceEvent, accEvent);
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
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
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
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
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
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		// buying 0.001 BTC with price 45000
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("SELL").price(new BigDecimal("45000"))
				.priceOfLastFilledTrade(new BigDecimal("45000")).originalQuantity(new BigDecimal("0.001"))
				.commission(new BigDecimal("1")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.0015"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("318"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		// transfering 0.003 ETH <-- 0.0003 BTC
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH + BTC).orderStatus("FILLED").side("TRANSFER").price(new BigDecimal("0.1"))
				.priceOfLastFilledTrade(new BigDecimal("0.1")).originalQuantity(new BigDecimal("0.003"))
				.commission(new BigDecimal("0.00001")).commissionAsset(BTC).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.00119"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(ETH, new BigDecimal("0.103"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		// transfering 0.1 BNB <-- 0.001 ETH
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BNB + ETH).orderStatus("FILLED").side("TRANSFER").price(new BigDecimal("0.01"))
				.priceOfLastFilledTrade(new BigDecimal("0.01")).originalQuantity(new BigDecimal("0.1"))
				.commission(new BigDecimal("0.0001")).commissionAsset(BTC).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(new AccountPositionUpdateEvent.Asset(BNB, new BigDecimal("0.1"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(ETH, new BigDecimal("0.1019"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
		// withdrawing 0.0009 ETH
		balanceEvent = BalanceUpdateEvent.builder().dateTime(dateTime).eventType(EventType.BALANCE_UPDATE).balances(ETH)
				.balanceDelta(new BigDecimal("-0.0009")).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List
						.of(new AccountPositionUpdateEvent.Asset(ETH, new BigDecimal("0.101"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		calcAlgorithm.processBalanceUpdate(incomeState, balanceEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(balanceEvent);
		aelist.add(accEvent);
		// selling 0.00119 BTC with price 50000
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("SELL").price(new BigDecimal("50000"))
				.priceOfLastFilledTrade(new BigDecimal("50000")).originalQuantity(new BigDecimal("0.00119"))
				.commission(new BigDecimal("1.5")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("376"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
	}

	@Test
	public void shouldReturnCorrectBalanceStatesForAllAssets() throws SecurityException, IllegalArgumentException {
		SpotBalanceCalcAlgorithm alg = new SpotBalanceCalcAlgorithm();
		List<SpotIncomeState> acceptedBSlist = alg.processEvents(aelist);
		// TODO краще вручну пройтися і порівняти параметри. наприклад
		// bsList[0]=accepted[0] і т.д.
		// якщо щось зафейлиться, то у нас буде строка яка зафейлилася і відразу буде
		// видно на якому етапі проблема
		assertIterableEquals(bsList, acceptedBSlist);
	}
	
	@Test
	public void shouldCorrectlyHandleConvert() {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse("2021-08-16 00:01:00", dateFormat);
		OrderTradeUpdateEvent orderEvent;
		AccountPositionUpdateEvent accEvent;
		SpotIncomeState incomeState;
		
		incomeState = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(ETH, new BigDecimal("2")), new Asset(BTC, new BigDecimal("0")))), 
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("1"), new BigDecimal("4000")))), 
				Collections.emptyList(), 
				Collections.emptyList());
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + ETH).orderStatus("FILLED").side("BUY").price(new BigDecimal("10"))
				.priceOfLastFilledTrade(new BigDecimal("10")).originalQuantity(new BigDecimal("0.1"))
				.commission(new BigDecimal("0")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.1"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(ETH, new BigDecimal("1"), new BigDecimal("0"))))
				.build();
		calcAlgorithm.handleConvertOperation(incomeState, orderEvent, accEvent);
		assertEquals(new BigDecimal("0.1"), incomeState.findLockedAsset(BTC).get().getBalance());
		assertEquals(new BigDecimal("0"), incomeState.findLockedAsset(ETH).get().getBalance());
		assertEquals(new BigDecimal("1"), incomeState.findAssetOpt(ETH).get().getBalance());
		
		orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + ETH).orderStatus("FILLED").side("SELL").price(new BigDecimal("0.1"))
				.priceOfLastFilledTrade(new BigDecimal("0.1")).originalQuantity(new BigDecimal("0.1"))
				.commission(new BigDecimal("0")).commissionAsset(USDT).build();
		accEvent = AccountPositionUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ACCOUNT_POSITION_UPDATE)
				.balances(List.of(
						new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0.0"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(ETH, new BigDecimal("2"), new BigDecimal("0"))))
				.build();
		calcAlgorithm.handleConvertOperation(incomeState, orderEvent, accEvent);
		assertEquals(new BigDecimal("0.0"), incomeState.findLockedAsset(BTC).get().getBalance());
		assertEquals(new BigDecimal("1"), incomeState.findLockedAsset(ETH).get().getBalance());
		assertEquals(new BigDecimal("2"), incomeState.findAssetOpt(ETH).get().getBalance());
	}
}