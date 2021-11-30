package com.example.binanceparser.config;

import com.binance.api.client.FuturesIncomeType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IncomeConfig extends Config {
    private int limit;
    private String symbol;
    private FuturesIncomeType incomeType;
}
