package com.example.binanceparser.domain;

import com.example.binanceparser.domain.SpotIncomeState.LockedAsset;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
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
        final LockedAsset usdt1 = new LockedAsset(USDT, new BigDecimal("10"), null, new BigDecimal("1"));
        final LockedAsset usdt2 = new LockedAsset(USDT, new BigDecimal("100"), null, new BigDecimal("1"));
        final LockedAsset eth1 = new LockedAsset(ETH, new BigDecimal("1"), null, new BigDecimal("4000"));
        final LockedAsset eth2 = new LockedAsset(BTC, new BigDecimal("0.1"), null, new BigDecimal("50000"));
        state1 = new SpotIncomeState(null, new LinkedHashSet<>(List.of(usdt1, eth1)), emptyList(), emptyList());
        state2 = new SpotIncomeState(null, new LinkedHashSet<>(List.of(usdt2, eth2)), emptyList(), emptyList());
    }

    @Test
    public void shouldClalculateUSDBalanceForAllAssets() {
        assertEquals(new BigDecimal("4010"), state1.calculateVirtualUSDBalance());
        assertEquals(new BigDecimal("5100.0"), state2.calculateVirtualUSDBalance());
    }

    @Test
    public void shouldCalculateUSDBalanceForOneAsset() {
        assertEquals(new BigDecimal("4000"), state1.calculateVirtualUSDBalance(ETH));
        assertEquals(new BigDecimal("100"), state2.calculateVirtualUSDBalance(USDT));
    }

    @Test
    public void shouldCorrectlyHandleBuy() {
        SpotIncomeState state;

        //buying 0.1 ETH for 5000 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.1")), new Asset(USDT, new BigDecimal("20100")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("4000")), new LockedAsset(USDT, new BigDecimal("20100"), new BigDecimal("1")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(ETH, new BigDecimal("0.1"), new BigDecimal("5000"));
        assertEquals(new BigDecimal("4500"), state.findLockedAsset(ETH).get().getAverageQuotePrice());
        assertEquals(new BigDecimal("0.2"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
        assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //buying 0.1 ETH for 6000 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.3")), new Asset(USDT, new BigDecimal("19500")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.2"), new BigDecimal("4500")), new LockedAsset(USDT, new BigDecimal("19500"), new BigDecimal("1")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(ETH, new BigDecimal("0.1"), new BigDecimal("6000"));
        assertEquals(new BigDecimal("5000"), state.findLockedAsset(ETH).get().getAverageQuotePrice());
        assertEquals(new BigDecimal("0.3"), state.findLockedAsset(ETH).get().getBalance());
        assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
        assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //buying 0.1 BTC for 45000 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.4")), new Asset(USDT, new BigDecimal("15000")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.3"), new BigDecimal("5000")), new LockedAsset(USDT, new BigDecimal("15000"), new BigDecimal("1")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(BTC, new BigDecimal("0.1"), new BigDecimal("45000"));
        assertEquals(new BigDecimal("45000"), state.findLockedAsset(BTC).get().getAverageQuotePrice());
        assertEquals(new BigDecimal("0.1"), state.findLockedAsset("BTC").get().getBalance());
        assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
        assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //buying 0.3 BTC for 50000 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.4")), new Asset(BTC, new BigDecimal("0.1")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.3"), new BigDecimal("5000")), new LockedAsset(BTC, new BigDecimal("0.1"), new BigDecimal("45000")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(BTC, new BigDecimal("0.3"), new BigDecimal("50000"));
        assertEquals(new BigDecimal("48750"), state.findLockedAsset(BTC).get().getAverageQuotePrice());
        assertEquals(new BigDecimal("0.4"), state.findLockedAsset(BTC).get().getBalance());
        assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
        assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
    }

    @Test
    public void shouldCorrectlyHandleSell() {
        SpotIncomeState state;
        /*список assets содержит название монет и их количество ПОСЛЕ трейда
         * список locked assets должен содержать информацию по заловенным ассетам ДО трейда
         * когда мы вызываем метод update assets, мы перезаписываем инфу в lockedAssets для стейблкоинов
         * получается, при продаже монеты за USDT у нас одновременно будет содержаться в locked списке и полученные USDT и еще не проданный ассет
         * более того, virt_usd тоже не обновлен до момента вызова метода
         */
        //selling 0.2 ETH for 3500 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.2")), new Asset(BTC, new BigDecimal("0.4")), new Asset(USDT, new BigDecimal("700")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.3"), new BigDecimal("5000")), new LockedAsset(BTC, new BigDecimal("0.4"), new BigDecimal("48750")), new LockedAsset(USDT, new BigDecimal("700"), new BigDecimal("1")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(ETH, new BigDecimal("-0.2"), new BigDecimal("3500"));
        assertEquals(new BigDecimal("5000"), state.findLockedAsset(ETH).get().getAverageQuotePrice());
        assertEquals(new BigDecimal("0.1"), state.findLockedAsset(ETH).get().getBalance());//asset balance
        assertEquals(new BigDecimal("-300.0"), state.getBalanceState());//overall income (-300 on this transaction)
        assertEquals(new BigDecimal("20700.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        //selling 0.4 BTC for 40000 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("20700")), new Asset(ETH, new BigDecimal("0.2")), new Asset(USDT, new BigDecimal("16700")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("5000")), new LockedAsset(BTC, new BigDecimal("0.4"), new BigDecimal("48750")), new LockedAsset(USDT, new BigDecimal("16700"), new BigDecimal("1")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("-300"));
        state.processOrderDetails(BTC, new BigDecimal("-0.4"), new BigDecimal("40000"));
        assertEquals(null, state.findLockedAsset(BTC));//asset balance, null because it was deleted after we sold all available amount
        assertEquals(new BigDecimal("-3800.0"), state.getBalanceState());//overall income (-3500 on this transaction)
        assertEquals(new BigDecimal("17200.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance

        /*при продаже больше чем было залоченноб например, как в примере ниже, в USDT баланс идет вся сумма из торга и потом она копируется в locked assets
         * также при пересчете будет взяна вся сумма из USDT из locked assets, поэтому этот тест фейлится, нам надо учесть только сумму торга залоченного
         */

        //selling 0.2 ETH for 3600 per 1
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("17200")), new Asset(USDT, new BigDecimal("17420")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("5000")), new LockedAsset(USDT, new BigDecimal("17420"), new BigDecimal("1")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("-3800"));
        state.processOrderDetails(ETH, new BigDecimal("-0.2"), new BigDecimal("3600.0"));
        assertEquals(null, state.findLockedAsset(ETH));//asset balance, null because it was deleted after we sold all available amount
        assertEquals(new BigDecimal("-3940.00"), state.getBalanceState());//overall income (-140 on this transaction)
        assertEquals(new BigDecimal("17060"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
    }

    @Test
    public void shouldCorrectlyHandleWithdraw() {
        SpotIncomeState state;

        //withdrawing 0.1 BTC
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("5400")), new Asset(ETH, new BigDecimal("0.2")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("4000")), new LockedAsset(BTC, new BigDecimal("0.1"), new BigDecimal("50000")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(BTC, new BigDecimal("-0.1"), null);
        assertEquals(null, state.findLockedAsset(BTC));//asset balance, null because it was deleted after we withdrawed all available amount
        assertEquals(new BigDecimal("0.0"), state.getBalanceState());//overall income (0, because we withdraw with current avg price)
        assertEquals(new BigDecimal("5400"), state.findAsset(VIRTUAL_USD).getBalance());

        //withdrawing 0.2 ETH
        state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("5400")))),
                new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("4000")))),
                Lists.emptyList(), Lists.emptyList());
        state.setBalanceState(new BigDecimal("0"));
        state.processOrderDetails(ETH, new BigDecimal("-0.2"), null);
        assertEquals(null, state.findAsset(ETH));//asset balance, null because it was deleted after we withdrawed all available amount
        assertEquals(new BigDecimal("0.0"), state.getBalanceState());
        assertEquals(new BigDecimal("5400"), state.findAsset(VIRTUAL_USD).getBalance());
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
    	return new AccountPositionUpdateEvent.Asset(asset.getAsset(), asset.getBalance(), ZERO);
	}

	private static BigDecimal num(String val){
    	return new BigDecimal(val);
	}

	private static LockedAsset locked(String assetName, String balance, String stableValue){
    	return locked(assetName, new BigDecimal(balance), new BigDecimal(stableValue));
	}

	private static LockedAsset locked(String assetName, BigDecimal balance, BigDecimal stableValue){
		return new LockedAsset(assetName, balance, stableValue);
	}

	private static Asset asset(String assetName, BigDecimal balance){
		return new Asset(assetName, balance);
	}

	private static Asset asset(String assetName, String balance){
    	return new Asset(assetName, new BigDecimal(balance));
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