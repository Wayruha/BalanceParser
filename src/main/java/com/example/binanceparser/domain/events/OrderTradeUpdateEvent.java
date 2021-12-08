package com.example.binanceparser.domain.events;

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
