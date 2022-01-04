package com.example.binanceparser.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.example.binanceparser.domain.SpotIncomeState.AssetState;
import static com.example.binanceparser.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

public class SpotIncomeStateTest {
	private static SpotIncomeState state1;
	private static SpotIncomeState state2;
	
	@BeforeAll
	public static void init() {
		state1 = new SpotIncomeState(null, new LinkedHashSet<AssetState>(List.of(new AssetState(USDT, new BigDecimal("10"), new BigDecimal("1")), new AssetState(ETH, new BigDecimal("1"), new BigDecimal("4000")))), null);
		state2 = new SpotIncomeState(null, new LinkedHashSet<AssetState>(List.of(new AssetState(USDT, new BigDecimal("100"), new BigDecimal("1")), new AssetState(BTC, new BigDecimal("0.1"), new BigDecimal("50000")))), null);
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
}
