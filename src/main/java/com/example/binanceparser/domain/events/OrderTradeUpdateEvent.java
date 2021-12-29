package com.example.binanceparser.domain.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.example.binanceparser.Constants.*;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderTradeUpdateEvent extends AbstractEvent {

	private String symbol;

	private String orderStatus;

	private BigDecimal price;

	private BigDecimal priceOfLastFilledTrade;

	private BigDecimal originalQuantity;

	private String side;

	private BigDecimal commission;

	private String commissionAsset;

	private Long orderId;

	public BigDecimal getAquiredQuantity() {
		return commissionAsset != null && commissionAsset.equals(getBaseAsset()) ? originalQuantity.subtract(commission)
				: originalQuantity;
	}

	public BigDecimal getPriceIncludingCommission() {
		if (orderStatus.equals("FILLED")) {
			if (side.equals("BUY")) {
				if (commissionAsset.equals(getQuoteAsset())) {
					return priceOfLastFilledTrade.add(commission.divide(getAquiredQuantity(), MATH_CONTEXT));
				} else if (commissionAsset.equals(getBaseAsset())) {
					return priceOfLastFilledTrade.multiply(originalQuantity).divide(getAquiredQuantity(), MATH_CONTEXT);
				} else {
					return priceOfLastFilledTrade;
				}
			} else if (side.equals("SELL")) {
				if (commissionAsset.equals(getQuoteAsset())) {
					return priceOfLastFilledTrade.subtract(commission.divide(getAquiredQuantity(), MATH_CONTEXT));
				} else if (commissionAsset.equals(getBaseAsset())) {
					return priceOfLastFilledTrade.multiply(getAquiredQuantity()).divide(originalQuantity, MATH_CONTEXT);
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
		if (side.equals("BUY")) {
			return getAquiredQuantity();
		} else if (side.equals("SELL")) {
			return getAquiredQuantity().negate();
		} else {
			throw new IllegalArgumentException("Unrecognized order.Side");
		}
	}

	public String getBaseAsset() {
		return EXCHANGE_INFO.getSymbolInfo(symbol).getBaseAsset();
	}

	public String getQuoteAsset() {
		return EXCHANGE_INFO.getSymbolInfo(symbol).getQuoteAsset();
	}
}