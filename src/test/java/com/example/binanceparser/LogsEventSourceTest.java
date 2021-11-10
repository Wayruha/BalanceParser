package com.example.binanceparser;

import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.example.binanceparser.domain.events.EventType.FUTURES_ORDER_TRADE_UPDATE;
import static org.junit.jupiter.api.Assertions.*;

public class LogsEventSourceTest {
    final LogsEventSource eventSource = new LogsEventSource();

    @Test
    public void testParseLogLine() throws JsonProcessingException {
        final String testLogLine = "FUTURES_PRODUCER_Kozhukhar FUTURES_ORDER_TRADE_UPDATE : UserDataUpdateEvent[content=FuturesTradeEvent[symbol=BTCUSDT,side=BUY,type=TAKE_PROFIT_MARKET,isReduceOnly=true,executionType=EXPIRED,originalQuantity=0.12,price=1.3,orderStatus=EXPIRED,quantityLastFilledTrade=0,accumulatedQuantity=0,priceOfLastFilledTrade=0,orderId=31456096465,newClientOrderId=ios_mzudq8unKoGaOs5msaXg,eventTime=1631668318189,timeInForce=GTE_GTC,commission=,commissionAsset=,orderTradeTime=1631668318172,tradeId=0,bidsNotional=0,askNotional=0,isMaker=false,stopPriceWorkingType=MARK_PRICE,originalOrderType=TAKE_PROFIT_MARKET,positionSide=SHORT,isCloseAll=true,activationPrice=,callbackRate=,realizedTradeProfit=0,eventType=FUTURES_ORDER_TRADE_UPDATE],eventType=FUTURES_ORDER_TRADE_UPDATE,type=ORDER_TRADE_UPDATE]";
        final AbstractEvent _event = eventSource.parseLogLine(LocalDateTime.now(), testLogLine);

        assertEquals(FUTURES_ORDER_TRADE_UPDATE, _event.getEventType());
        assertEquals("FUTURES_PRODUCER_Kozhukhar", _event.getSource());
        final FuturesOrderTradeUpdateEvent event = (FuturesOrderTradeUpdateEvent) _event;
        System.out.println(event.isReduceOnly());
        assertEquals("BTCUSDT", event.getSymbol());
        assertEquals(0.12, event.getOriginalQuantity());
        assertEquals("EXPIRED", event.getOrderStatus());
        assertEquals(1.3, event.getPrice());
        assertTrue(event.isReduceOnly());
    }
}
