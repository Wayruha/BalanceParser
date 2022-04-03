package com.example.binanceparser.domain.events;

import com.binance.api.client.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import static com.binance.api.client.domain.OrderSide.BUY;
import static com.binance.api.client.domain.OrderSide.SELL;
import static com.example.binanceparser.Constants.EXCHANGE_INFO;
import static com.example.binanceparser.Constants.MATH_CONTEXT;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class OrderTradeUpdateEvent extends OrderEvent {
    public BigDecimal getActualBaseQty() {
        return commissionAsset != null && commissionAsset.equals(getBaseAsset()) ? originalQuantity.subtract(commission)
                : originalQuantity;
    }

    public BigDecimal getOriginalQuoteQty() {
        return originalQuantity.multiply(price);
    }

    public BigDecimal getActualQuoteQty() {
        return commissionAsset != null && commissionAsset.equals(getQuoteAsset()) ? getOriginalQuoteQty().subtract(commission)
                : getOriginalQuoteQty();
    }

    public BigDecimal getPriceIncludingCommission() {
        if (orderStatus == OrderStatus.FILLED) {
            if (side == BUY) {
                if (commissionAsset.equals(getQuoteAsset())) {
                    return priceOfLastFilledTrade.add(commission.divide(getActualBaseQty(), MATH_CONTEXT));
                } else if (commissionAsset.equals(getBaseAsset())) {
                    return priceOfLastFilledTrade.multiply(originalQuantity).divide(getActualBaseQty(), MATH_CONTEXT);
                } else {
                    return priceOfLastFilledTrade;
                }
            } else if (side == SELL) {
                if (commissionAsset.equals(getQuoteAsset())) {
                    return priceOfLastFilledTrade.subtract(commission.divide(getActualBaseQty(), MATH_CONTEXT));
                } else if (commissionAsset.equals(getBaseAsset())) {
                    return priceOfLastFilledTrade.multiply(getActualBaseQty()).divide(originalQuantity, MATH_CONTEXT);
                } else {
                    return priceOfLastFilledTrade;
                }
            } else {
                throw new IllegalArgumentException("Unrecognized order.Side");
            }
        } else {
            return priceOfLastFilledTrade;
        }
    }

    public BigDecimal getTradeDelta() {
        if (side == BUY) {
            return getActualBaseQty();
        } else if (side == SELL) {
            return getActualBaseQty().negate();
        } else {
            throw new IllegalArgumentException("Unrecognized order.Side");
        }
    }

    public BigDecimal getQuoteAssetQty() {
        return getActualBaseQty().multiply(priceOfLastFilledTrade);
    }

    public BigDecimal getQuoteAssetCommission() {
        return getPriceIncludingCommission().subtract(getPriceOfLastFilledTrade()).multiply(originalQuantity).abs();
    }

    public String getBaseAsset() {
        return EXCHANGE_INFO.getSymbolInfo(symbol).getBaseAsset();
    }

    public String getQuoteAsset() {
        return EXCHANGE_INFO.getSymbolInfo(symbol).getQuoteAsset();
    }
}