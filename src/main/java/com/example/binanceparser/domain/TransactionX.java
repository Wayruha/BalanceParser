package com.example.binanceparser.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public abstract class TransactionX {
    final TransactionType type;
    final BigDecimal valueIncome;

    public TransactionX(TransactionType type, BigDecimal valueIncome) {
        this.type = type;
        this.valueIncome = valueIncome;
    }

    @Builder
    @Getter
    public static class Asset2 {
        String assetName;
        BigDecimal txQty;
        BigDecimal fullBalance;
        BigDecimal valuableBalance;
        BigDecimal stableValue;
    }
}


