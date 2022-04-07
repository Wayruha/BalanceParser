package com.example.binanceparser.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsReport {
    private StatisticType type;
    private Statistics stats;
    private String chartPath;

    public StatsReport(StatisticType type) {
        this.type = type;
    }
}
