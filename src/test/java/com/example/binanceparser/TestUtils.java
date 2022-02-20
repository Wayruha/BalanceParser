package com.example.binanceparser;

import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public class TestUtils {
    public static AccountPositionUpdateEvent.Asset eventAsset(String asset, BigDecimal free, BigDecimal locked) {
        return new AccountPositionUpdateEvent.Asset(asset, free, locked);
    }

    public static AccountPositionUpdateEvent.Asset eventAsset(String asset, String free, String locked) {
        return new AccountPositionUpdateEvent.Asset(asset, num(free), num(locked));
    }

    public static ArrayList emptyList() {
        return new ArrayList<>();
    }

    public static LinkedHashSet setOf(Object... args) {
        return new LinkedHashSet<>(List.of(args));
    }

    public static BigDecimal num(String val) {
        return new BigDecimal(val);
    }

    public static BalanceUpdateEvent balanceUpdateEvent(String asset, BigDecimal assetDelta) {
        final BalanceUpdateEvent balanceUpdateEvent = new BalanceUpdateEvent(asset, asset, assetDelta);
        balanceUpdateEvent.setDateTime(LocalDateTime.now());
        return balanceUpdateEvent;
    }

    public static AccountPositionUpdateEvent accountUpdateEvent(AccountPositionUpdateEvent.Asset... assets) {
        final AccountPositionUpdateEvent accEvent = new AccountPositionUpdateEvent(List.of(assets));
        return accEvent;
    }

    public static AccountPositionUpdateEvent.Asset toEventAsset(Asset asset) {
        return eventAsset(asset.getAsset(), asset.getBalance(), ZERO);
    }


}
