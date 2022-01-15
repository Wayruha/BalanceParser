package com.example.binanceparser.domain;

import com.example.binanceparser.domain.SpotIncomeState.LockedAsset;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.util.Lists.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpotIncomeStateTest {
    private static SpotIncomeState state1;
    private static SpotIncomeState state2;

    @BeforeAll
    public static void init() {
        final LockedAsset usdt1 = new LockedAsset(USDT, num("10"), null, num("1"));
        final LockedAsset usdt2 = new LockedAsset(USDT, num("100"), null, num("1"));
        final LockedAsset eth1 = new LockedAsset(ETH, num("1"), null, num("4000"));
        final LockedAsset eth2 = new LockedAsset(BTC, num("0.1"), null, num("50000"));
        state1 = new SpotIncomeState(null, setOf(usdt1, eth1), emptyList(), emptyList());
        state2 = new SpotIncomeState(null, setOf(usdt2, eth2), emptyList(), emptyList());
    }

    @Test
    public void shouldClalculateUSDBalanceForAllAssets() {
        assertEquals(num("4010"), state1.calculateVirtualUSDBalance());
        assertEquals(num("5100.0"), state2.calculateVirtualUSDBalance());
    }

    @Test
    public void shouldCalculateUSDBalanceForOneAsset() {
        assertEquals(num("4000"), state1.calculateVirtualUSDBalance(ETH));
        assertEquals(num("100"), state2.calculateVirtualUSDBalance(USDT));
    }

    @Test
    public void shouldCorrectlyHandleBuy() {
    	OrderTradeUpdateEvent orderEvent;
		AccountPositionUpdateEvent accEvent;
        SpotIncomeState state;

        //buying 0.1 ETH for 5000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH + USDT).orderStatus("FILLED").side("BUY").price(num("5000"))
				.priceOfLastFilledTrade(num("5000")).originalQuantity(num("0.1"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(ETH, num("0.2"), num("0")),
						eventAsset(USDT, num("20100"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.1")), asset(USDT, num("20600"))),
                setOf(locked(ETH, num("0.1"), num("400")), locked(USDT, num("20600"), num("20600"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("0"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(num("900"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("0.2"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("0"), state.getBalanceState());//overall income
        assertEquals(num("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //buying 0.1 ETH for 6000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH + USDT).orderStatus("FILLED").side("BUY").price(num("6000"))
				.priceOfLastFilledTrade(num("6000")).originalQuantity(num("0.1"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(ETH, num("0.3"), num("0")),
						eventAsset(USDT, num("19500"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.2")), asset(USDT, num("20100"))),
                setOf(locked(ETH, num("0.2"), num("900")), locked(USDT, num("20100"), num("20100"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("0"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(num("1500"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("0.3"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("0"), state.getBalanceState());//overall income
        assertEquals(num("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //buying 0.1 BTC for 45000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("BUY").price(num("45000"))
				.priceOfLastFilledTrade(num("45000")).originalQuantity(num("0.1"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(BTC, num("0.1"), num("0")),
						eventAsset(USDT, num("15000"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.3")), asset(USDT, num("19500"))),
                setOf(locked(ETH, num("0.3"), num("1500")), locked(USDT, num("19500"), num("19500"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("0"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(num("4500"), state.findLockedAsset(BTC).get().getStableValue());
        assertEquals(num("0.1"), state.findLockedAsset("BTC").get().getBalance());
        assertEquals(num("0"), state.getBalanceState());//overall income
        assertEquals(num("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //buying 0.3 BTC for 50000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("BUY").price(num("50000"))
				.priceOfLastFilledTrade(num("50000")).originalQuantity(num("0.3"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(BTC, num("0.4"), num("0")),
						eventAsset(USDT, num("0"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.3")), asset(BTC, num("0.1")), asset(USDT, num("15000"))),
                setOf(locked(ETH, num("0.3"), num("1500")), locked(BTC, num("0.1"), num("4500")), locked(USDT, num("15000"), num("15000"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("0"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(num("19500"), state.findLockedAsset(BTC).get().getStableValue());
        assertEquals(num("0.4"), state.findLockedAsset(BTC).get().getBalance());
        assertEquals(num("0"), state.getBalanceState());//overall income
        assertEquals(num("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
    }

    @Test
    public void shouldCorrectlyHandleSell() {
        SpotIncomeState state;
        OrderTradeUpdateEvent orderEvent;
		AccountPositionUpdateEvent accEvent;
        /*список assets содержит название монет и их количество ПОСЛЕ трейда
         * список locked assets должен содержать информацию по заловенным ассетам ДО трейда
         * когда мы вызываем метод update assets, мы перезаписываем инфу в lockedAssets для стейблкоинов
         * получается, при продаже монеты за USDT у нас одновременно будет содержаться в locked списке и полученные USDT и еще не проданный ассет
         * более того, virt_usd тоже не обновлен до момента вызова метода
         */
        //selling 0.2 ETH for 3500 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH + USDT).orderStatus("FILLED").side("SELL").price(num("3500"))
				.priceOfLastFilledTrade(num("3500")).originalQuantity(num("0.2"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(ETH, num("0.2"), num("0")),
						eventAsset(USDT, num("700"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.4")), asset(BTC, num("0.4")), asset(USDT, num("0"))),
                setOf(locked(ETH, num("0.3"), num("1500")), locked(BTC, num("0.4"), num("19500")), locked(USDT, num("0"), num("0"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("0"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(num("500"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(num("0.1"), state.findLockedAsset(ETH).get().getBalance());//asset balance
        assertEquals(num("-300.0"), state.getBalanceState());//overall income (-300 on this transaction)
        assertEquals(num("20700.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //selling 0.4 BTC for 40000 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(BTC + USDT).orderStatus("FILLED").side("SELL").price(num("40000"))
				.priceOfLastFilledTrade(num("40000")).originalQuantity(num("0.4"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(BTC, num("0.0"), num("0")),
						eventAsset(USDT, num("16700"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("20700")), asset(ETH, num("0.2")), asset(BTC, num("0.4")), asset(USDT, num("700"))),
                setOf(locked(ETH, num("0.1"), num("500")), locked(BTC, num("0.4"), num("19500")), locked(USDT, num("700"), num("700"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("-300"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(null, state.findLockedAsset(BTC));//asset balance, null because it was deleted after we sold all available amount
        assertEquals(num("-3800.0"), state.getBalanceState());//overall income (-3500 on this transaction)
        assertEquals(num("17200.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        /*при продаже больше чем было залоченноб например, как в примере ниже, в USDT баланс идет вся сумма из торга и потом она копируется в locked assets
         * также при пересчете будет взяна вся сумма из USDT из locked assets, поэтому этот тест фейлится, нам надо учесть только сумму торга залоченного
         */

        //selling 0.2 ETH for 3600 per 1
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
				.symbol(ETH + USDT).orderStatus("FILLED").side("SELL").price(num("3600"))
				.priceOfLastFilledTrade(num("3600")).originalQuantity(num("0.2"))
				.commission(num("0.0")).commissionAsset(USDT).build();
		accEvent = accountUpdateEvent(
						eventAsset(ETH, num("0.0"), num("0")),
						eventAsset(USDT, num("17420"), num("0")));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("17200")), asset(ETH, num("0.2")), asset(USDT, num("16700"))),
                setOf(locked(ETH, num("0.1"), num("500")), locked(USDT, num("16700"), num("16700"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("-3800"));
        state.processOrder(orderEvent, accEvent);
        assertEquals(null, state.findLockedAsset(ETH));//asset balance, null because it was deleted after we sold all available amount
        assertEquals(num("-3940.00"), state.getBalanceState());//overall income (-140 on this transaction)
        assertEquals(num("17060"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
    }

    @Test
    public void shouldCorrectlyHandleWithdraw() {
        SpotIncomeState state;
        BalanceUpdateEvent balanceEvent;
		AccountPositionUpdateEvent accEvent;
        
        //withdrawing 0.1 BTC
		balanceEvent = balanceUpdateEvent(BTC, num("-0.1"));
		accEvent = accountUpdateEvent(toEventAsset(asset(BTC, num("0.0"))));
		state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("5400")), asset(BTC, num("0.1")), asset(ETH, num("0.2"))),
				setOf(locked(ETH, num("0.1"), num("400")), locked(BTC, num("0.1"), num("5000"))),
				emptyList(),
				emptyList());
        
        state.setBalanceState(num("0"));
        state.processBalanceUpdate(balanceEvent, accEvent);
        assertEquals(null, state.findLockedAsset(BTC).get());//asset balance, null because it was deleted after we withdrawed all available amount
        assertEquals(num("0.0"), state.getBalanceState());//overall income (0, because we withdraw with current avg price)
        assertEquals(num("5400"), state.findAsset(VIRTUAL_USD).getBalance());

        //withdrawing 0.2 ETH
        balanceEvent = balanceUpdateEvent(ETH, num("-0.2"));
		accEvent = accountUpdateEvent(toEventAsset(asset(ETH, num("0.0"))));
        state = new SpotIncomeState(setOf(asset(VIRTUAL_USD, num("5400")), asset(ETH, num("0.2"))),
                setOf(locked(ETH, num("0.1"), num("4000"))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(num("0"));
        state.processBalanceUpdate(balanceEvent, accEvent);
        assertEquals(null, state.findLockedAsset(ETH).get());//asset balance, null because it was deleted after we withdrawed all available amount
        assertEquals(num("0.0"), state.getBalanceState());
        assertEquals(num("5400"), state.findAsset(VIRTUAL_USD).getBalance());
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
		state.processBalanceUpdate(balanceEvent, accEvent);
		//depositing 0.5 ETH
        assertEquals(ethBalBefore, state.findLockedAsset(ETH).get().getBalance());
        assertEquals(num("800"), state.findLockedAsset(ETH).get().getStableValue());
        assertEquals(ethBalAfter, state.findAssetOpt(ETH).get().getBalance());
        assertEquals(num("900"), state.findAsset(VIRTUAL_USD).getBalance());

        //depositing 0.1 BTC
		final BigDecimal btcBefore = num("0.1");
		final BigDecimal depositBtcQty = num("0.2");
        final BigDecimal btcBalanceAfter = btcBefore.add(depositBtcQty);
        final Asset btcAfter = asset(BTC, btcBalanceAfter);

        final BalanceUpdateEvent balanceEvent2 = balanceUpdateEvent(BTC, depositBtcQty);
        final AccountPositionUpdateEvent accEvent2 = accountUpdateEvent(toEventAsset(btcAfter));

		state.processBalanceUpdate(balanceEvent2, accEvent2);
        assertTrue(state.findLockedAsset(BTC).isEmpty());
        assertEquals(btcBalanceAfter, state.findAsset(BTC).getBalance());
        assertEquals(num("900"), state.findAsset(VIRTUAL_USD).getBalance());
    }
    
    @Test
    public void shouldCorrectlyHandleSubject() {
    	
    }

    private static BalanceUpdateEvent balanceUpdateEvent(String asset, BigDecimal assetDelta){
		final BalanceUpdateEvent balanceUpdateEvent = new BalanceUpdateEvent(asset, asset, assetDelta);
		balanceUpdateEvent.setDateTime(LocalDateTime.now());
		return balanceUpdateEvent;
	}

	private static AccountPositionUpdateEvent accountUpdateEvent(AccountPositionUpdateEvent.Asset... assets){
		final AccountPositionUpdateEvent accEvent = new AccountPositionUpdateEvent(List.of(assets));
		return accEvent;
	}

	private static AccountPositionUpdateEvent.Asset toEventAsset(Asset asset){
    	return eventAsset(asset.getAsset(), asset.getBalance(), ZERO);
	}

	private static BigDecimal num(String val){
    	return new BigDecimal(val);
	}

	private static LockedAsset locked(String assetName, String balance, String stableValue){
    	return new LockedAsset(assetName, num(balance), num(stableValue));
	}

	private static LockedAsset locked(String assetName, BigDecimal balance, BigDecimal stableValue){
		return new LockedAsset(assetName, balance, stableValue);
	}

	private static Asset asset(String assetName, BigDecimal balance){
		return new Asset(assetName, balance);
	}

	private static Asset asset(String assetName, String balance){
    	return new Asset(assetName, num(balance));
	}

	private static AccountPositionUpdateEvent.Asset eventAsset(String asset, BigDecimal free, BigDecimal locked){
		return new AccountPositionUpdateEvent.Asset(asset, free, locked);
	}

	private static AccountPositionUpdateEvent.Asset eventAsset(String asset, String free, String locked){
    	return new AccountPositionUpdateEvent.Asset(asset, num(free), num(locked));
	}

	private static ArrayList emptyList(){
    	return new ArrayList<>();
	}

	private static LinkedHashSet setOf(Object... args){
        return new LinkedHashSet(List.of(args));
    }
}