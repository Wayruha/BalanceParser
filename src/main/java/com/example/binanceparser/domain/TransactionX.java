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

    public static Trade buyTx(LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income, BigDecimal valuableBaseQtyInvolved) {
        return new Trade(BUY, date, baseAsset, quoteAsset, income, valuableBaseQtyInvolved);
    }

    public static Trade sellTx(LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income, BigDecimal valuableBaseQtyInvolved) {
        return new Trade(SELL, date, baseAsset, quoteAsset, income, valuableBaseQtyInvolved);
    }

    public static Trade convertTx(LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal valuableBaseQtyInvolved) {
        return new Trade(CONVERT, date, baseAsset, quoteAsset, BigDecimal.ZERO, valuableBaseQtyInvolved);
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
        BigDecimal valuableBaseQtyInvolved;
        
        protected Trade(TransactionType type, LocalDateTime date, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal valueIncome, BigDecimal valuableBaseQtyInvolved) {
            super(type, date, valueIncome);
            this.baseAsset = baseAsset;
            this.quoteAsset = quoteAsset;
            this.valuableBaseQtyInvolved = valuableBaseQtyInvolved;
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


