package com.example.binanceparser;

import com.binance.api.client.FuturesIncomeType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IncomeConfig extends Config {
    //TODO code should be formatted!
    //TODO 2 field should be private!
    private int limit;
    private String symbol;
    private FuturesIncomeType incomeType;
}
