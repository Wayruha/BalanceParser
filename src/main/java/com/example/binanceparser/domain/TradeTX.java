package com.example.binanceparser.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import static com.example.binanceparser.domain.TransactionType.BUY;
import static com.example.binanceparser.domain.TransactionType.SELL;

@Getter
@Setter
class TradeTX extends TransactionX {
    Asset2 baseAsset;
    Asset2 quoteAsset;

    protected TradeTX(TransactionType type, Asset2 baseAsset, Asset2 quoteAsset, BigDecimal valueIncome) {
        super(type, valueIncome);
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
    }

    public static TradeTX buyTx(Asset2 baseAsset, Asset2 quoteAsset) {
        return new TradeTX(BUY, baseAsset, quoteAsset, BigDecimal.ZERO);
    }

    public static TradeTX sellTx(Asset2 baseAsset, Asset2 quoteAsset, BigDecimal income) {
        return new TradeTX(SELL, baseAsset, quoteAsset, income);
    }
}
