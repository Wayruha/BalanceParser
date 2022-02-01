package com.example.binanceparser.report;

import com.example.binanceparser.domain.TransactionX;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private int totalTxCount;
    private int totalTradeTxCount;

    private String user;
    private List<TransactionX> transactions;

    public String toPrettyString() {
        final StringBuilder bldr = new StringBuilder("Result for ").append(user).append(":\n");
        return bldr.append("Start date: ").append(startTrackDate).append("\n")
                .append("FinishDate: ").append(finishTrackDate).append("\n")
                .append("Balance at start: ").append(balanceAtStart).append("\n")
                .append("Balance at end: ").append(balanceAtEnd).append("\n")
                .append("Min balance: ").append(min).append("\n")
                .append("Max balance: ").append(max).append("\n")
                .append("Balance Delta: ").append(balanceDifference).append("\n")
                .append("Total transactions: ").append(totalTxCount).append("\n")
                .append("Total trade transactions: ").append(totalTradeTxCount).append("\n")
                .append("Chart: ").append(outputPath).append("\n")
                .toString();
    }

    public String json() throws JsonProcessingException {
        return new ObjectMapper().writer().writeValueAsString(this);
    }
}