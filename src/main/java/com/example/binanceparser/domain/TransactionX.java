package com.example.binanceparser.domain;

import java.math.BigDecimal;

public abstract class TransactionX {
    final TransactionType type;
    final BigDecimal valueIncome;

    public TransactionX(TransactionType type, BigDecimal valueIncome) {
        this.type = type;
        this.valueIncome = valueIncome;
    }
}


