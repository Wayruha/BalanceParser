package com.example.binanceparser.domain.events;

import com.example.binanceparser.domain.events.AbstractEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderTradeUpdateEvent extends AbstractEvent {


    String symbol;

    String orderStatus;

    BigDecimal price;// цена за одну единицу symbol

    Double originalQuantity;// колличество покупки/продажи

    String side;// покупка/продажа и т.д.

    BigDecimal commission;

    String commissionAsset;

    Long eventTime;

    Long orderId;

    Double accumulatedQuantity;


    //[eventTime=1629094686500,symbol=BTCUSDT,
    // side=SELL,type=LIMIT,orderStatus=FILLED,executionType=TRADE,price=47296.97000000,originalQuantity=0.00023500,
    // cumulativeQuoteQty=11.11478795,lastQuoteQty=11.11478795,quoteOrderQty=0.00000000,newClientOrderId=ios_8506b3bca7ea45c39eb12f2d77e83ff6,
    // orderId=7190563160,timeInForce=GTC,orderRejectReason=NONE,quantityLastFilledTrade=0.00023500,accumulatedQuantity=0.00023500,
    // priceOfLastFilledTrade=47296.97000000,commission=0.01111479,commissionAsset=USDT,orderTradeTime=1629094686499,tradeId=1007613538,
    // orderCreationTime=1629094676522,eventType=ORDER_TRADE_UPDATE],eventType=ORDER_TRADE_UPDATE,type=executionReport]
}
