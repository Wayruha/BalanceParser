package com.example.binanceparser.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import static com.example.binanceparser.Constants.MATH_CONTEXT;
import static java.math.BigDecimal.ZERO;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class LockedAsset extends Asset {
    private String quoteAsset;
    // price of this asset relative to quoteAsset. E.g., relativeAsset = USD
    private BigDecimal averageQuotePrice;
    private BigDecimal stableValue;

    public LockedAsset(String asset, BigDecimal availableBalance, String quoteAsset, BigDecimal stableValue) {
        super(asset, availableBalance);
        this.quoteAsset = quoteAsset;
        this.stableValue = stableValue;
    }

    public LockedAsset(String asset, BigDecimal availableBalance, BigDecimal stableValue) {
        super(asset, availableBalance);
        this.quoteAsset = null;
        this.averageQuotePrice = null;
        this.stableValue = stableValue;
    }

    public static LockedAsset empty(String assetName) {
        return new LockedAsset(assetName, ZERO, null, ZERO);
    }

    public LockedAsset clone() {
        LockedAsset asset = new LockedAsset(super.asset, super.balance, quoteAsset, stableValue);
        asset.setQuoteAsset(quoteAsset);
        return asset;
    }

    public void deductBalance(BigDecimal qty) {
        if (qty.signum() == 0) return;
        if (balance.compareTo(qty) < 0)
            System.out.println("Deducting more than exists. qty=" + qty.toPlainString() + ". " + toString());
        final BigDecimal balanceBefore = balance;
        balance = balance.subtract(qty);
        //value -= value * (qty/balance)
        stableValue = stableValue.subtract(stableValue.multiply(qty).divide(balanceBefore, MATH_CONTEXT));
    }

    public void addBalance(BigDecimal qty, BigDecimal stableValue) {
        if (stableValue.signum() == 0) {
            throw new IllegalArgumentException("stableValue can't be null in LockedAsset");
        }
        this.balance = this.balance.add(qty);
        this.stableValue = this.stableValue.add(stableValue);
    }
}
