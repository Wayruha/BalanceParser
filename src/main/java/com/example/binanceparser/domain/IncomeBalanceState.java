package com.example.binanceparser.domain;

import com.binance.api.client.FuturesIncomeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeBalanceState extends BalanceState {
    FuturesIncomeType incomeType;
    BigDecimal availableBalance;

    public IncomeBalanceState(LocalDate dateTime, BigDecimal availableBalance, FuturesIncomeType incomeType) {
        super(dateTime);
        this.availableBalance = availableBalance;
        this.incomeType = incomeType;
    }

}
