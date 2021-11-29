package com.example.binanceparser;

import com.binance.api.client.FuturesIncomeType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class IncomeConfig extends Config {

    int limit;
    String symbol;
    FuturesIncomeType incomeType;

    public IncomeConfig(LocalDateTime startTrackDate, LocalDateTime finishTrackDate, String inputFilepath,
                        String outputDir, String symbol, FuturesIncomeType incomeType,int limit) {
        super(startTrackDate, finishTrackDate, inputFilepath, outputDir);
        this.incomeType = incomeType;
        this.symbol = symbol;
        this.limit = limit;
    }
}
