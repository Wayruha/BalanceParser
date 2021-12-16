package com.example.binanceparser.domain;

import com.binance.api.client.FuturesIncomeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class IncomeBalanceState extends BalanceState {
    private FuturesIncomeType incomeType;

    public IncomeBalanceState(LocalDateTime dateTime, BigDecimal availableBalance, FuturesIncomeType incomeType) {
        super(availableBalance, dateTime);
        this.incomeType = incomeType;
    }

}
