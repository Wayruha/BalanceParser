package com.example.binanceparser.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.example.binanceparser.domain.SpotIncomeState.LockedAsset;
import static com.example.binanceparser.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class SpotIncomeStateTest {
	private static SpotIncomeState state1;
	private static SpotIncomeState state2;
	
	@BeforeAll
	public static void init() {
		state1 = new SpotIncomeState(null, new LinkedHashSet<>(List.of(new LockedAsset(USDT, new BigDecimal("10"), new BigDecimal("1")), new LockedAsset(ETH, new BigDecimal("1"), new BigDecimal("4000")))), null);
		state2 = new SpotIncomeState(null, new LinkedHashSet<>(List.of(new LockedAsset(USDT, new BigDecimal("100"), new BigDecimal("1")), new LockedAsset(BTC, new BigDecimal("0.1"), new BigDecimal("50000")))), null);
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
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(ETH, new BigDecimal("0.1"), new BigDecimal("5000"));
		assertEquals(new BigDecimal("4500"), state.findLockedAsset(ETH).getAveragePrice());
		assertEquals(new BigDecimal("0.2"), state.findLockedAsset(ETH).getBalance());
		assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
		assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
		 
		//buying 0.1 ETH for 6000 per 1
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.3")), new Asset(USDT, new BigDecimal("19500")))),
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.2"), new BigDecimal("4500")), new LockedAsset(USDT, new BigDecimal("19500"), new BigDecimal("1")))),
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(ETH, new BigDecimal("0.1"), new BigDecimal("6000"));
		assertEquals(new BigDecimal("5000"), state.findLockedAsset(ETH).getAveragePrice());
		assertEquals(new BigDecimal("0.3"), state.findLockedAsset(ETH).getBalance());
		assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
		assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
		
		//buying 0.1 BTC for 45000 per 1
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.4")), new Asset(USDT, new BigDecimal("15000")))), 
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.3"), new BigDecimal("5000")), new LockedAsset(USDT, new BigDecimal("15000"), new BigDecimal("1")))),
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(BTC, new BigDecimal("0.1"), new BigDecimal("45000"));
		assertEquals(new BigDecimal("45000"), state.findLockedAsset(BTC).getAveragePrice());
		assertEquals(new BigDecimal("0.1"), state.findLockedAsset("BTC").getBalance());
		assertEquals(new BigDecimal("0"), state.getBalanceState());//overall income
		assertEquals(new BigDecimal("21000.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
		
		//buying 0.3 BTC for 50000 per 1
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("21000")), new Asset(ETH, new BigDecimal("0.4")), new Asset(BTC, new BigDecimal("0.1")))),
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.3"), new BigDecimal("5000")), new LockedAsset(BTC, new BigDecimal("0.1"), new BigDecimal("45000")))),
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(BTC, new BigDecimal("0.3"), new BigDecimal("50000"));
		assertEquals(new BigDecimal("48750"), state.findLockedAsset(BTC).getAveragePrice());
		assertEquals(new BigDecimal("0.4"), state.findLockedAsset(BTC).getBalance());
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
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(ETH, new BigDecimal("-0.2"), new BigDecimal("3500"));
		assertEquals(new BigDecimal("5000"), state.findLockedAsset(ETH).getAveragePrice());
		assertEquals(new BigDecimal("0.1"), state.findLockedAsset(ETH).getBalance());//asset balance
		assertEquals(new BigDecimal("-300.0"), state.getBalanceState());//overall income (-300 on this transaction)
		assertEquals(new BigDecimal("20700.0"), state.findAsset(VIRTUAL_USD).getBalance());//virtual balance
		
		//selling 0.4 BTC for 40000 per 1
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("20700")), new Asset(ETH, new BigDecimal("0.2")), new Asset(USDT, new BigDecimal("16700")))), 
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("5000")), new LockedAsset(BTC, new BigDecimal("0.4"), new BigDecimal("48750")), new LockedAsset(USDT, new BigDecimal("16700"), new BigDecimal("1")))), 
				new ArrayList<>());
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
				new ArrayList<>());
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
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));	
		state.processOrderDetails(BTC, new BigDecimal("-0.1"), null);
		assertEquals(null, state.findLockedAsset(BTC));//asset balance, null because it was deleted after we withdrawed all available amount
		assertEquals(new BigDecimal("0.0"), state.getBalanceState());//overall income (0, because we withdraw with current avg price)
		assertEquals(new BigDecimal("5400"), state.findAsset(VIRTUAL_USD).getBalance());
		
		//withdrawing 0.2 ETH
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("5400")))), 
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.1"), new BigDecimal("4000")))),
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(ETH, new BigDecimal("-0.2"), null);
		assertEquals(null, state.findAsset(ETH));//asset balance, null because it was deleted after we withdrawed all available amount
		assertEquals(new BigDecimal("0.0"), state.getBalanceState());
		assertEquals(new BigDecimal("5400"), state.findAsset(VIRTUAL_USD).getBalance());
	}
	
	@Test
	public void shouldCorrectlyHandleDeposit() {
		SpotIncomeState state;
		
		//depositing 0.5 ETH
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("800")), new Asset(ETH, new BigDecimal("0.7")))),
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.2"), new BigDecimal("4000")))), 
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(ETH, new BigDecimal("0.5"), null);
		assertEquals(new BigDecimal("0.2"), state.findLockedAsset(ETH).getBalance());
		assertEquals(new BigDecimal("4000"), state.findLockedAsset(ETH).getAveragePrice());
		assertEquals(new BigDecimal("800.0"), state.findAsset(VIRTUAL_USD).getBalance());
		
		//depositing 0.1 BTC
		state = new SpotIncomeState(new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, new BigDecimal("800")), new Asset(ETH, new BigDecimal("0.7")), new Asset(BTC, new BigDecimal("0.1")))), 
				new LinkedHashSet<>(List.of(new LockedAsset(ETH, new BigDecimal("0.2"), new BigDecimal("4000")))), 
				new ArrayList<>());
		state.setBalanceState(new BigDecimal("0"));
		state.processOrderDetails(BTC, new BigDecimal("0.1"), null);
		assertEquals(null, state.findLockedAsset(BTC));
		assertEquals(new BigDecimal("800.0"), state.findAsset(VIRTUAL_USD).getBalance());
	}
}