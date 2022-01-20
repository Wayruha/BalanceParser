package com.example.binanceparser.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import static com.example.binanceparser.Utils.format;
import static com.example.binanceparser.domain.TransactionType.*;

@Getter
@Setter
public abstract class TransactionX {
    final TransactionType type;
    final BigDecimal valueIncome;

    public TransactionX(TransactionType type, BigDecimal valueIncome) {
        this.type = type;
        this.valueIncome = valueIncome;
    }

    public static Trade buyTx(Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income) {
        return new Trade(BUY, baseAsset, quoteAsset, income);
    }

    public static Trade sellTx(Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income) {
        return new Trade(SELL, baseAsset, quoteAsset, income);
    }

    public static Trade convertTx(Asset2 baseAsset, Asset2 quoteAsset) {
        return new Trade(CONVERT, baseAsset, quoteAsset, BigDecimal.ZERO);
    }

    public static Update depositTx(Asset2 asset, BigDecimal income) {
        return new Update(DEPOSIT, asset, income);
    }

    public static Update withdrawTx(Asset2 asset, BigDecimal income) {
        return new Update(WITHDRAW, asset, income);
    }

    @Override
    public String toString() {
        return "TX{" +
                "type=" + type +
                ", valueIncome=" + format(valueIncome) +
                '}';
    }

    @Builder
    @Getter
    public static class Asset2 {
        String assetName;
        BigDecimal txQty;
        BigDecimal fullBalance;
        BigDecimal valuableBalance;
        BigDecimal stableValue;

        @Override
        public String toString() {
            return "Asset{" +
                    "assetName='" + assetName + '\'' +
                    ", txQty=" + format(txQty) +
                    ", fullBalance=" + format(fullBalance) +
                    ", valuableBalance=" + format(valuableBalance) +
                    ", stableValue=" + format(stableValue) +
                    '}';
        }
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class Trade extends TransactionX {
        Asset2 baseAsset;
        Asset2 quoteAsset;

        protected Trade(TransactionType type, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal valueIncome) {
            super(type, valueIncome);
            this.baseAsset = baseAsset;
            this.quoteAsset = quoteAsset;
        }
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class Update extends TransactionX {
        Asset2 asset;

        protected Update(TransactionType type, Asset2 asset, BigDecimal income) {
            super(type, income);
            this.asset = asset;
        }
    }
}


