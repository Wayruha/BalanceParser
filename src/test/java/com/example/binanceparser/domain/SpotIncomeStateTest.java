package com.example.binanceparser.domain;

import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpotIncomeStateTest {
    private static SpotIncomeState state1;
    private static SpotIncomeState state2;
    private static SpotBalanceCalcAlgorithm calcAlgorithm;

    @BeforeAll
    public static void init() {
        final LockedAsset usdt1 = new LockedAsset(USDT, num("10"), null, num("10"));
        final LockedAsset usdt2 = new LockedAsset(USDT, num("100"), null, num("100"));
        final LockedAsset eth1 = new LockedAsset(ETH, num("1"), null, num("4000"));
        final LockedAsset eth2 = new LockedAsset(BTC, num("0.1"), null, num("5000"));
        state1 = new SpotIncomeState(Set.of(), setOf(usdt1, eth1), emptyList(), emptyList());
        state2 = new SpotIncomeState(Set.of(), setOf(usdt2, eth2), emptyList(), emptyList());
        calcAlgorithm = new SpotBalanceCalcAlgorithm();
    }

    @Test
    public void shouldClalculateUSDBalanceForAllAssets() {
        assertEquals(num("4010"), state1.calculateVirtualUSDBalance());
        assertEquals(num("5100"), state2.calculateVirtualUSDBalance());
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
    public void shouldCorrectlyHandleConvert() {
        OrderTradeUpdateEvent orderEvent;
        AccountPositionUpdateEvent accEvent;
        SpotIncomeState state;
        //transfering 0.1 ETH <-- 0.01 BTC
        //TODO просто поміняв місцями стейт і аккІвент. Ідея в тому що спочатку був певний баланс (стейт) а потім прийшов аккІвент з новими балансами монет
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(BTC, num("0.04")), asset(ETH, num("0.1")), asset(USDT, num("19100"))),
                setOf(locked(ETH, num("0.1"), num("400")), locked(BTC, num("0.03"), num("1500")), locked(USDT, num("19100"), num("19100"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).side("BUY").eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + BTC).orderStatus("FILLED").side("TRANSFER").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("0.1")).commission(num("0"))
                .commissionAsset(BTC).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.2"), num("0")), eventAsset(BTC, num("0.03"), num("0")));

        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("21000"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
        assertEquals(num("0.2"), state.findAssetOpt(ETH).get().getBalance());
        assertEquals(num("0.03"), state.findAssetOpt(BTC).get().getBalance());
        assertEquals(num("0.2"), state.findLockedAsset(ETH).get().getBalance());// TODO should be 0.2 but 0.3
        assertEquals(num("0.02"), state.findLockedAsset(BTC).get().getBalance());

        //transfering 0.25 ETH<-- 0.025 BTC
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(ETH, num("0.2")), asset(BTC, num("0.03")), asset(USDT, num("19100"))),
                setOf(locked(ETH, num("0.2"), num("900")), locked(BTC, num("0.02"), num("1000")), locked(USDT, num("19100"), num("19100"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).side("BUY").eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(ETH + BTC).orderStatus("FILLED").side("TRANSFER").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("0.25")).commission(num("0"))
                .commissionAsset(BTC).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.45"), num("0")), eventAsset(BTC, num("0.005"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("21000"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
        assertEquals(num("0.4"), state.findLockedAsset(ETH).get().getBalance());//should be 0.4 but 0.45 (only 0.02 BTC of 0.025 is legal)
        assertTrue(state.findLockedAsset(BTC).isEmpty());//transfered all legal BTC to ETH

        //transfering 1 AXS <-- 0.005 BTC
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(BTC, num("0.005")), asset(ETH, num("0.45")), asset(USDT, num("19100"))),
                setOf(locked(ETH, num("0.4"), num("1900")), locked(USDT, num("19100"), num("19100"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).side("BUY").eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(AXS + BTC).orderStatus("FILLED").side("TRANSFER").price(num("0.005"))
                .priceOfLastFilledTrade(num("0.005")).originalQuantity(num("1")).commission(num("0"))
                .commissionAsset(BTC).build();
        accEvent = accountUpdateEvent(eventAsset(BTC, num("0.0"), num("0")), eventAsset(AXS, num("1"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("21000"), state.findAssetOpt(VIRTUAL_USD).get().getBalance());
        assertTrue(state.findLockedAsset(AXS).isEmpty());//because was bought for illegal asset

        //transfering 1 BNB <-- 0.1 ETH
        state = new SpotIncomeState(
                setOf(asset(VIRTUAL_USD, num("21000")), asset(BTC, num("0.0")), asset(ETH, num("0.45")), asset(AXS, num("1")), asset(USDT, num("19100"))),
                setOf(locked(ETH, num("0.4"), num("1900")), locked(USDT, num("19100"), num("19100"))), emptyList(), emptyList());
        orderEvent = OrderTradeUpdateEvent.builder().dateTime(null).eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BNB + ETH).orderStatus("FILLED").side("TRANSFER").price(num("0.1"))
                .priceOfLastFilledTrade(num("0.1")).originalQuantity(num("1")).commission(num("0"))
                .commissionAsset(ETH).build();
        accEvent = accountUpdateEvent(eventAsset(ETH, num("0.35"), num("0")), eventAsset(BNB, num("1"), num("0")));
        calcAlgorithm.processOrder(state, orderEvent, accEvent);
        assertEquals(num("1"), state.findLockedAsset(BNB).get().getBalance());
        assertEquals(num("0.3"), state.findLockedAsset(ETH).get().getBalance());
    }

    private static BalanceUpdateEvent balanceUpdateEvent(String asset, BigDecimal assetDelta) {
        final BalanceUpdateEvent balanceUpdateEvent = new BalanceUpdateEvent(asset, asset, assetDelta);
        balanceUpdateEvent.setDateTime(LocalDateTime.now());
        return balanceUpdateEvent;
    }

    private static AccountPositionUpdateEvent accountUpdateEvent(AccountPositionUpdateEvent.Asset... assets) {
        final AccountPositionUpdateEvent accEvent = new AccountPositionUpdateEvent(List.of(assets));
        return accEvent;
    }

    private static AccountPositionUpdateEvent.Asset toEventAsset(Asset asset) {
        return eventAsset(asset.getAsset(), asset.getBalance(), ZERO);
    }

    private static BigDecimal num(String val) {
        return new BigDecimal(val);
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

    private static AccountPositionUpdateEvent.Asset eventAsset(String asset, BigDecimal free, BigDecimal locked) {
        return new AccountPositionUpdateEvent.Asset(asset, free, locked);
    }

    private static AccountPositionUpdateEvent.Asset eventAsset(String asset, String free, String locked) {
        return new AccountPositionUpdateEvent.Asset(asset, num(free), num(locked));
    }

    private static ArrayList emptyList() {
        return new ArrayList<>();
    }

    private static LinkedHashSet setOf(Object... args) {
        return new LinkedHashSet(List.of(args));
    }
}