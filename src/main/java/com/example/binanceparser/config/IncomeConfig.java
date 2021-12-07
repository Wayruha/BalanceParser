package com.example.binanceparser.config;

import com.binance.api.client.FuturesIncomeType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class IncomeConfig extends Config {
    private int limit;
    private String symbol;
    private List<FuturesIncomeType> incomeType;
}
