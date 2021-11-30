package com.example.binanceparser.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BalanceReport {
    private LocalDateTime startTrackDate;
    private LocalDateTime finishTrackDate;
    private BigDecimal balanceAtStart;
    private BigDecimal balanceAtEnd;
    private BigDecimal max;
    private BigDecimal min;
    private String outputPath;
    private BigDecimal balanceDifference;

    public String toPrettyString() {
        final StringBuilder bldr = new StringBuilder("Result:\n");
        return bldr.append("Start date: ").append(startTrackDate).append("\n")
                .append("FinishDate: ").append(finishTrackDate).append("\n")
                .append("Balance at start: ").append(balanceAtStart).append("\n")
                .append("Balance at end: ").append(balanceAtEnd).append("\n")
                .append("Min balance: ").append(min).append("\n")
                .append("Max balance: ").append(max).append("\n")
                .append("Balance Delta: ").append(balanceDifference).append("\n")
                .append("Chart: ").append(outputPath).append("\n")
                .toString();
    }
}
