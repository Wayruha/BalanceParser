package com.example.binanceparser.domain.events;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.example.binanceparser.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTradeUpdateEventTest {
    @Test
    public void shouldReturnCorrectAcquiredQuantity() {
        OrderTradeUpdateEvent orderEvent = OrderTradeUpdateEvent.builder()
                .symbol(BTC + USDT)
                .originalQuantity(new BigDecimal("0.001"))
                .commission(new BigDecimal("1"))
                .commissionAsset(USDT)
                .build();
        assertEquals(new BigDecimal("0.001"), orderEvent.getAcquiredQuantity());
        orderEvent = OrderTradeUpdateEvent.builder()
                .symbol(BTC + USDT)
                .originalQuantity(new BigDecimal("0.0011"))
                .commission(new BigDecimal("0.0001"))
                .commissionAsset(BTC)
                .build();
        assertEquals(new BigDecimal("0.0010"), orderEvent.getAcquiredQuantity());
    }

    @Test
    public void shouldReturnCorrectPriceIncludingCommission() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse("2021-08-16 00:01:00", dateFormat);
        OrderTradeUpdateEvent orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("NEW")
                .side("BUY").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.001"))
                .commission(new BigDecimal("0.5"))
                .commissionAsset(USDT).build();
        assertEquals(new BigDecimal("45000"), orderEvent.getPriceIncludingCommission());
        orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("FILLED")
                .side("BUY").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.001"))
                .commission(new BigDecimal("0.5"))
                .commissionAsset(USDT).build();
        assertEquals(new BigDecimal("45500"), orderEvent.getPriceIncludingCommission());
        orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("FILLED")
                .side("BUY").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.0011"))
                .commission(new BigDecimal("0.0001"))
                .commissionAsset(BTC).build();
        assertEquals(new BigDecimal("49500"), orderEvent.getPriceIncludingCommission());
        orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("FILLED")
                .side("BUY").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.001"))
                .commission(new BigDecimal("0.1"))
                .commissionAsset(BNB).build();
        assertEquals(new BigDecimal("45000"), orderEvent.getPriceIncludingCommission());
        orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("FILLED")
                .side("SELL").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.001"))
                .commission(new BigDecimal("0.5"))
                .commissionAsset(USDT).build();
        assertEquals(new BigDecimal("44500"), orderEvent.getPriceIncludingCommission());
        orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("FILLED")
                .side("SELL").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.0011"))
                .commission(new BigDecimal("0.0001"))
                .commissionAsset(BTC).build();
        assertEquals(new BigDecimal("40909.090"), orderEvent.getPriceIncludingCommission());
        orderEvent = OrderTradeUpdateEvent.builder()
                .dateTime(dateTime)
                .eventType(EventType.ORDER_TRADE_UPDATE)
                .symbol(BTC + USDT)
                .orderStatus("FILLED")
                .side("SELL").price(new BigDecimal("45000"))
                .priceOfLastFilledTrade(new BigDecimal("45000"))
                .originalQuantity(new BigDecimal("0.001"))
                .commission(new BigDecimal("0.1"))
                .commissionAsset(BNB).build();
        assertEquals(new BigDecimal("45000"), orderEvent.getPriceIncludingCommission());
    }
}