package com.example.binanceparser.domain.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderTradeUpdateEvent extends AbstractEvent {


    private String symbol;

    private String orderStatus;

    private BigDecimal price;

    private BigDecimal priceOfLastFilledTrade;

    private Double originalQuantity;

    private String side;

    private BigDecimal commission;

    private String commissionAsset;

    private Long eventTime;

    private Long orderId;

}
