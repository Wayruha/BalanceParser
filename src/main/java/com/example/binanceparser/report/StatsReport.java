package com.example.binanceparser.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsReport {
    private String priceChartPath;
    private String delayChartPath;
    private String incomeChartPath;

    public String toPrettyString() {
        StringBuilder str = new StringBuilder();
        str.append("priceChartPath").append(":").append(priceChartPath)
                .append("delayChartPath").append(":").append(delayChartPath)
                .append("incomeChartPath").append(":").append(incomeChartPath);
        return str.toString();
    }
}
