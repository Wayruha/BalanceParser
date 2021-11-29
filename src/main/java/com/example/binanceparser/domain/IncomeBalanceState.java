package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeBalanceState extends BalanceState {
    private IncomeType incomeType;
    private BigDecimal availableBalance;

    public IncomeBalanceState(LocalDate dateTime, BigDecimal availableBalance, IncomeType incomeType) {
        super(dateTime);
        this.availableBalance = availableBalance;
        this.incomeType = incomeType;
    }

}
