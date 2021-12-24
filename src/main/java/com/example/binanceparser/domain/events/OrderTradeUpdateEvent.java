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

	// Double->BigDecimal may cause some buds when parsing event, should check if
	// this change is safe
	private BigDecimal originalQuantity;

	private String side;

	private BigDecimal commission;

	private String commissionAsset;

	private Long orderId;

	public String getOrderSymbol() {
		return symbol.replace(USDT, "");
	}

	public BigDecimal getAquiredQuantity() {
		return commissionAsset != null && commissionAsset.equals(getOrderSymbol())
				? originalQuantity.subtract(commission)
				: originalQuantity;
	}

	public BigDecimal getPriceIncludingCommission() {
		if (orderStatus.equals("FILLED")) {
			if (side.equals("BUY")) {
				if (commissionAsset.equals(USDT)) {
					return price.add(commission.divide(getAquiredQuantity(), MATH_CONTEXT));
				} else if (commissionAsset.equals(getOrderSymbol())) {
					return price.multiply(originalQuantity).divide(getAquiredQuantity(), MATH_CONTEXT);
				} else {
					return price;
				}
			} else if (side.equals("SELL")) {
				if (commissionAsset.equals(USDT)) {
					return price.subtract(commission.divide(getAquiredQuantity(), MATH_CONTEXT));
				} else if (commissionAsset.equals(getOrderSymbol())) {
					return price.multiply(getAquiredQuantity()).divide(originalQuantity, MATH_CONTEXT);
				} else {
					return price;
				}
			} else {
				throw new IllegalArgumentException("Unrecognized order.Side");
			}
		} else {
			return price;
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

	public String getQuoteAsset() {
		if (side.equals("BUY")) {
			return USDT;
		} else if (side.equals("SELL")) {
			return getOrderSymbol();
		} else {
			throw new IllegalArgumentException("Unrecognized order.Side");
		}
	}
}