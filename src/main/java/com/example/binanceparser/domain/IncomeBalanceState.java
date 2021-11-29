package com.example.binanceparser.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeBalanceState extends BalanceState {

    IncomeType incomeType;

    BigDecimal availableBalance;

    public IncomeBalanceState(LocalDate dateTime, BigDecimal availableBalance, IncomeType incomeType) {
        super(dateTime);
        this.availableBalance = availableBalance;
        this.incomeType = incomeType;
    }

}
