package com.example.binanceparser.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.example.binanceparser.Utils.format;
import static com.example.binanceparser.domain.TransactionType.*;

@Getter
@Setter
public abstract class TransactionX {
    protected final TransactionType type;
    protected final BigDecimal valueIncome;
    protected LocalDateTime date;

    public TransactionX(TransactionType type, LocalDateTime date, BigDecimal valueIncome) {
        this.type = type;
        this.valueIncome = valueIncome;
        this.date = date;
    }

    public static Trade buyTx(LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income) {
        return new Trade(BUY, date, baseAsset, quoteAsset, income);
    }

    public static Trade sellTx(LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income) {
        return new Trade(SELL, date, baseAsset, quoteAsset, income);
    }

    public static Trade convertTx(LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset) {
        return new Trade(CONVERT, date, baseAsset, quoteAsset, BigDecimal.ZERO);
    }

    public static Update depositTx(LocalDateTime date, Asset2 asset, BigDecimal income) {
        return new Update(DEPOSIT, date, asset, income);
    }

    public static Update withdrawTx(LocalDateTime date, Asset2 asset, BigDecimal income) {
        return new Update(WITHDRAW, date, asset, income);
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

        protected Trade(TransactionType type, LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal valueIncome) {
            super(type, date, valueIncome);
            this.baseAsset = baseAsset;
            this.quoteAsset = quoteAsset;
        }
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class Update extends TransactionX {
        Asset2 asset;

        protected Update(TransactionType type, LocalDateTime date, Asset2 asset, BigDecimal income) {
            super(type, date, income);
            this.asset = asset;
        }
    }
}


