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

    BigDecimal price;

    BigDecimal priceOfLastFilledTrade;

    Double originalQuantity;

    String side;

    BigDecimal commission;

    String commissionAsset;

    Long eventTime;

    Long orderId;

}
