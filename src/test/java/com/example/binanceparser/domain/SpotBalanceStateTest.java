package com.example.binanceparser.domain;

import com.example.binanceparser.domain.balance.SpotBalanceState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.binanceparser.Constants.*;
import static com.example.binanceparser.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpotBalanceStateTest {
    private static SpotBalanceState state1;
    private static SpotBalanceState state2;

    @BeforeAll
    public static void init() {
        final LockedAsset usdt1 = new LockedAsset(USDT, num("10"), null, num("10"));
        final LockedAsset usdt2 = new LockedAsset(USDT, num("100"), null, num("100"));
        final LockedAsset eth1 = new LockedAsset(ETH, num("1"), null, num("4000"));
        final LockedAsset eth2 = new LockedAsset(BTC, num("0.1"), null, num("5000"));
        state1 = new SpotBalanceState(Set.of(), setOf(usdt1, eth1), emptyList());
        state2 = new SpotBalanceState(Set.of(), setOf(usdt2, eth2), emptyList());
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
}