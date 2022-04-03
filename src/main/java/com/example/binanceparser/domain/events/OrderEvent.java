package com.example.binanceparser.domain.events;

import com.binance.api.client.domain.ExecutionType;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public abstract class OrderEvent extends AbstractEvent {
    protected String symbol;
    protected ExecutionType executionType;
    protected OrderStatus orderStatus;
    protected BigDecimal price;
    protected BigDecimal priceOfLastFilledTrade;
    protected BigDecimal originalQuantity;
    protected OrderSide side;
    protected Long orderId;
    protected String newClientOrderId;
    protected String actualOrderId;
    protected BigDecimal accumulatedQuantity;
    //protected OrderType type;
    //protected TimeInForce timeInForce;
    protected String quantityLastFilledTrade;
    protected String commissionAsset;
    protected BigDecimal commission;
    protected Long tradeId;
}
