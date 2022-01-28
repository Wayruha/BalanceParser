package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.LockedAsset;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.binanceparser.Constants.*;
import static com.example.binanceparser.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class SpotBalanceCalcAlgorithmTest {
    private static List<AbstractEvent> aelist = new ArrayList<>();
    private static List<SpotIncomeState> bsList = new ArrayList<>();
    private static SpotBalanceCalcAlgorithm calcAlgorithm = new SpotBalanceCalcAlgorithm();
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void shouldReturnCorrectBalanceStatesForAllAssets() throws SecurityException, IllegalArgumentException {
		final LocalDateTime dateTime = LocalDateTime.parse("2021-08-16 00:01:00", dateFormat);
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
				.symbol(ETH + BTC).orderStatus("FILLED").side("BUY").price(new BigDecimal("0.1"))
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
				.symbol(BNB + ETH).orderStatus("FILLED").side("BUY").price(new BigDecimal("0.01"))
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
				.balances(List.of(new AccountPositionUpdateEvent.Asset(BTC, new BigDecimal("0"), new BigDecimal("0")),
						new AccountPositionUpdateEvent.Asset(USDT, new BigDecimal("376"), new BigDecimal("0"))))
				.build();
		incomeState = new SpotIncomeState(dateTime, incomeState);
		calcAlgorithm.processOrder(incomeState, orderEvent, accEvent);
		bsList.add(incomeState);
		aelist.add(orderEvent);
		aelist.add(accEvent);
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

        incomeState = new SpotIncomeState(
                setOf(asset(ETH, num("2")), asset(BTC, num("1"))),
                setOf(locked(ETH, num("1"), num("4000"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + BTC).orderStatus("FILLED").side("SELL").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("1"))
                .commission(num("0")).commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(toEventAsset(asset(BTC, num("1.1"))), toEventAsset(asset(ETH, num("1"))));

        calcAlgorithm.handleConvertOperation(incomeState, orderEvent, accEvent);
        assertTrue(incomeState.findLockedAsset(ETH).isEmpty());
        assertEquals(num("1"), incomeState.findAssetOpt(ETH).get().getBalance());
        assertEquals(num("1.1"), incomeState.findAssetOpt(BTC).get().getBalance());
        assertEquals(num("0.1"), incomeState.findLockedAsset(BTC).get().getBalance());
        assertEquals(num("4000"), incomeState.findLockedAsset(BTC).get().getStableValue());

        orderEvent = OrderTradeUpdateEvent.builder().dateTime(dateTime).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + BTC).orderStatus("FILLED").side("BUY").price(num("0.2"))
                .priceOfLastFilledTrade(num("0.2")).originalQuantity(num("1"))
                .commission(num("0")).commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(toEventAsset(asset(BTC, num("0"))), toEventAsset(asset(ETH, num("2"))));
        calcAlgorithm.handleConvertOperation(incomeState, orderEvent, accEvent);
        assertTrue(incomeState.findLockedAsset(BTC).isEmpty());
        assertEquals(num("4000"), incomeState.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("0.5"), incomeState.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("2"), incomeState.findAssetOpt(ETH).get().getBalance());
    }

    @Test
    public void shouldCorrectlyHandleBuy() {
        OrderTradeUpdateEvent orderEvent;
        AccountPositionUpdateEvent accEvent;
        SpotIncomeState state;

        // buying 0.1 ETH for USDT. price: 5000 usdt
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + USDT).orderStatus("FILLED").side("BUY").price(num("5000"))
                .priceOfLastFilledTrade(num("5000")).originalQuantity(num("0.1")).commission(num("1"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.2"), num("0")), eventAsset(USDT, num("20100"), num("0")));
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.1")), asset(USDT, num("20600"))),
                setOf(locked(ETH, num("0.1"), num("400")), locked(USDT, num("20600"), num("20600"))), emptyList(),
                emptyList());
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("0.2"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("900.0"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("21000.0"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());// virtual balance
        assertEquals(num("-1.0"), state.getTXs().get(0).getValueIncome());// virtual balance

        // buying 0.1 ETH for USDT. price: 6000 usdt
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + USDT).orderStatus("FILLED").side("BUY").price(num("6000"))
                .priceOfLastFilledTrade(num("6000")).originalQuantity(num("0.1")).commission(num("5"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.3"), num("0")), eventAsset(USDT, num("19500"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("0.3"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("1500.0"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("21000.0"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());// virtual balance
        assertEquals(num("-5.0"), state.getTXs().get(1).getValueIncome());

        // buying 0.1 BTC for 45000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT).orderStatus("FILLED").side("BUY").price(num("45000"))
                .priceOfLastFilledTrade(num("45000")).originalQuantity(num("0.1")).commission(num("10"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(BTC, num("0.1"), num("0")), eventAsset(USDT, num("15000"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("0.1"), state.findLockedAsset("BTC").get().getBalance());
        assertEquals(num("4500.0"), state.findLockedAsset(BTC).get().getStableValue());
        assertEquals(num("21000.0"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());// virtual balance
        assertEquals(num("-10.0"), state.getTXs().get(2).getValueIncome());

        // buying 0.3 BTC for 50000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT).orderStatus("FILLED").side("BUY").price(num("50000"))
                .priceOfLastFilledTrade(num("50000")).originalQuantity(num("0.3")).commission(num("3"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(BTC, num("0.4"), num("0")), eventAsset(USDT, num("0"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("0.4"), state.findLockedAsset(BTC).get().getBalance());
        assertEquals(num("19500.0"), state.findLockedAsset(BTC).get().getStableValue());
        assertEquals(num("21000.0"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());// virtual balance
        assertEquals(num("-3.0"), state.getTXs().get(3).getValueIncome());
    }

    @Test
    public void shouldCorrectlyHandleSell() {
        SpotIncomeState state;
        OrderTradeUpdateEvent orderEvent;
        AccountPositionUpdateEvent accEvent;

        // selling 0.2 ETH for 3500 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + USDT).orderStatus("FILLED").side("SELL").price(num("3500"))
                .priceOfLastFilledTrade(num("3500")).originalQuantity(num("0.2")).commission(num("10"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.2"), num("0")), eventAsset(USDT, num("700"), num("0")));
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.4")), asset(BTC, num("0.4"))),
                setOf(locked(ETH, num("0.3"), num("1500")), locked(BTC, num("0.4"), num("19500"))), emptyList(), emptyList());
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("0.1"), state.findLockedAsset(ETH).get().getBalance());// asset balance
        assertEquals(num("500"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("20700"), state.findAssetOpt(VIRTUAL_USD).map(Asset::getBalance).orElse(BigDecimal.ZERO));
        //это противоречит предыдущему тесту, если -310, должно быть 21000-310=20690
        assertEquals(num("-310"), state.getTXs().get(0).getValueIncome().setScale(0)); // includes commission

        // selling 0.4 BTC for 40000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT).orderStatus("FILLED").side("SELL").price(num("40000"))
                .priceOfLastFilledTrade(num("40000")).originalQuantity(num("0.4")).commission(num("20"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(BTC, num("0.0"), num("0")), eventAsset(USDT, num("16700"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertTrue(state.findLockedAsset(BTC).isEmpty());// asset balance, null because it was deleted
        assertEquals(num("17200"), state.findAssetOpt(VIRTUAL_USD).map(Asset::getBalance).orElse(BigDecimal.ZERO).setScale(0));
        assertEquals(num("-3520"), state.getTXs().get(1).getValueIncome().setScale(0)); // includes commission

        // selling 0.15 ETH for 3000 per 1
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("17200")), asset(ETH, num("0.2")), asset(USDT, num("16700"))),
                setOf(locked(ETH, num("0.1"), num("500")), locked(USDT, num("16700"), num("16700"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + USDT).orderStatus("FILLED").side("SELL").price(num("3600"))
                .priceOfLastFilledTrade(num("3000")).originalQuantity(num("0.15")).commission(num("5"))
                .commissionAsset(USDT).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.05"), num("0")), eventAsset(USDT, num("17000"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertTrue(state.findLockedAsset(ETH).isEmpty());// asset balance, null because it was deleted
        assertEquals(num("0.05"), state.findAssetOpt(ETH).map(Asset::getBalance).orElse(BigDecimal.ZERO));// virtual balance
        assertEquals(num("17000"), state.findAssetOpt(VIRTUAL_USD).map(Asset::getBalance).orElse(BigDecimal.ZERO));// virtual balance
        assertEquals(num("17000"), state.findAssetOpt(USDT).map(Asset::getBalance).orElse(BigDecimal.ZERO));// virtual balance
        assertEquals(num("-205"), state.getTXs().get(0).getValueIncome().setScale(0, RoundingMode.HALF_UP)); // includes commission
    }

    @Test
    public void shouldCorrectlyHandleWithdraw() {
        SpotIncomeState state;
        BalanceUpdateEvent balanceEvent;
        AccountPositionUpdateEvent accEvent;

        // withdrawing 0.1 BTC
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("5400")), asset(ETH, num("0.2")), asset(BTC, num("0.1"))),
                setOf(locked(ETH, num("0.1"), num("400")), locked(BTC, num("0.1"), num("5000"))), emptyList(), emptyList());
        balanceEvent = balanceUpdateEvent(BTC, num("-0.1"));
        accEvent = accountUpdateEvent(toEventAsset(asset(BTC, num("0.0"))));

        calcAlgorithm.processBalanceUpdate(state, balanceEvent, accEvent);
        assertTrue(state.findLockedAsset(BTC).isEmpty());// asset balance, null because it was deleted after we withdrew all available amount
        assertEquals(num("400"), state.findLockedAsset(ETH).get().getStableValue());// asset balance, null because it was deleted after we withdrew all available amount
        assertEquals(num("-5000"), state.getTXs().get(0).getValueIncome());
        assertEquals(num("400"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());

        // withdrawing 0.05 ETH
        balanceEvent = balanceUpdateEvent(ETH, num("-0.05"));
        accEvent = accountUpdateEvent(toEventAsset(asset(ETH, num("0.15"))));
        calcAlgorithm.processBalanceUpdate(state, balanceEvent, accEvent);
        assertTrue(state.findLockedAsset(ETH).isPresent());// asset balance, present because it was not withdrawn completely
        assertEquals(num("0"), state.getTXs().get(1).getValueIncome());
        assertEquals(num("0.1"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("400"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());

        // withdrawing 0.15 ETH
        balanceEvent = balanceUpdateEvent(ETH, num("-0.15"));
        accEvent = accountUpdateEvent(toEventAsset(asset(ETH, num("0.0"))));
        calcAlgorithm.processBalanceUpdate(state, balanceEvent, accEvent);
        assertTrue(state.findLockedAsset(ETH).isEmpty()); //because withdrawed all amount
        assertEquals(num("-400.0"), state.getTXs().get(2).getValueIncome());
        assertEquals(num("0"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
    }

    @Test
    public void shouldCorrectlyHandleDeposit() {
        SpotIncomeState state;
        final Asset USDBefore = asset(VIRTUAL_USD, "900");
        final LockedAsset usdt = locked(USDT, num("100"), num("100"));
        final BigDecimal ethBalBefore = num("0.2");
        final BigDecimal depositEthQty = num("0.5");

        final Asset ethBefore = asset(ETH, ethBalBefore);
        final Set<Asset> currentAssets = setOf(USDBefore, ethBefore, usdt);
        final Set<LockedAsset> lockedAssets = setOf(usdt, locked(ETH, ethBalBefore, num("800")));
        state = new SpotIncomeState(currentAssets, lockedAssets, emptyList(), emptyList());

        final BigDecimal ethBalAfter = ethBalBefore.add(depositEthQty);
        final Asset ethAfter = asset(ETH, ethBalAfter);
        final BalanceUpdateEvent balanceEvent = balanceUpdateEvent(ETH, depositEthQty);
        final AccountPositionUpdateEvent accEvent = accountUpdateEvent(toEventAsset(ethAfter));
        calcAlgorithm.processBalanceUpdate(state, balanceEvent, accEvent);
        // depositing 0.5 ETH
        assertEquals(ethBalBefore, state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("800"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(ethBalAfter, state.findAssetOpt(ETH).get().getBalance());
        assertEquals(num("900"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());

        // depositing 0.1 BTC
        final BigDecimal btcBefore = num("0.1");
        final BigDecimal depositBtcQty = num("0.2");
        final BigDecimal btcBalanceAfter = btcBefore.add(depositBtcQty);
        final Asset btcAfter = asset(BTC, btcBalanceAfter);

        final BalanceUpdateEvent balanceEvent2 = balanceUpdateEvent(BTC, depositBtcQty);
        final AccountPositionUpdateEvent accEvent2 = accountUpdateEvent(toEventAsset(btcAfter));

        calcAlgorithm.processBalanceUpdate(state, balanceEvent2, accEvent2);
        assertTrue(state.findLockedAsset(BTC).isEmpty());
        assertEquals(btcBalanceAfter, state.findAssetOpt(BTC).get().getBalance());
        assertEquals(num("900"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
    }

    @Test
    public void shouldCorrectlyHandleConvert_2() {
        OrderTradeUpdateEvent orderEvent;
        AccountPositionUpdateEvent accEvent;
        SpotIncomeState state;
        //transfering 0.1 ETH <-- 0.01 BTC
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(BTC, num("0.04")), asset(ETH, num("0.1")), asset(USDT, num("19100"))),
                setOf(locked(ETH, num("0.1"), num("400")), locked(BTC, num("0.03"), num("1500")), locked(USDT, num("19100"), num("19100"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).side("BUY").eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + BTC).orderStatus("FILLED").side("BUY").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("0.1")).commission(num("0"))
                .commissionAsset(BTC).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.2"), num("0")), eventAsset(BTC, num("0.03"), num("0")));

        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("21000"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
        assertEquals(num("0.2"), state.findAssetOpt(ETH).get().getBalance());
        assertEquals(num("0.03"), state.findAssetOpt(BTC).get().getBalance());
        assertEquals(num("0.2"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("0.02"), state.findLockedAsset(BTC).get().getBalance());

        //transfering 0.25 ETH<-- 0.025 BTC
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).side("BUY").eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + BTC).orderStatus("FILLED").side("BUY").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("0.25")).commission(num("0"))
                .commissionAsset(BTC).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.45"), num("0")), eventAsset(BTC, num("0.005"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("21000"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
        assertEquals(num("0.400"), state.findLockedAsset(ETH).get().getBalance());//should be 0.4 but 0.45 (only 0.02 BTC of 0.025 is legal)
        assertTrue(state.findLockedAsset(BTC).isEmpty());//transfered all legal BTC to ETH

        //transfering 1 AXS <-- 0.005 BTC
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).side("BUY").eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(AXS + BTC).orderStatus("FILLED").side("BUY").price(num("0.005"))
                .priceOfLastFilledTrade(num("0.005")).originalQuantity(num("1")).commission(num("0"))
                .commissionAsset(BTC).build();
        accEvent = accountUpdateEvent(eventAsset(BTC, num("0.0"), num("0")), eventAsset(AXS, num("1"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("21000"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
        assertTrue(state.findLockedAsset(AXS).isEmpty());//because was bought for illegal asset

        //transfering 1 BNB <-- 0.1 ETH
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BNB + ETH).orderStatus("FILLED").side("BUY").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("1")).commission(num("0"))
                .commissionAsset(ETH).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.35"), num("0")), eventAsset(BNB, num("1"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("1"), state.findLockedAsset(BNB).get().getBalance());
        assertEquals(num("0.300"), state.findLockedAsset(ETH).get().getBalance());
    }

    private static LockedAsset locked(String assetName, String balance, String stableValue) {
        return new LockedAsset(assetName, num(balance), num(stableValue));
    }

    private static LockedAsset locked(String assetName, BigDecimal balance, BigDecimal stableValue) {
        return new LockedAsset(assetName, balance, stableValue);
    }

    private static Asset asset(String assetName, BigDecimal balance) {
        return new Asset(assetName, balance);
    }

    private static Asset asset(String assetName, String balance) {
        return new Asset(assetName, num(balance));
    }
}