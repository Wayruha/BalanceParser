package com.example.binanceparser.report;

import com.example.binanceparser.Utils;
import com.example.binanceparser.domain.transaction.Transaction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    @JsonSerialize(using = Utils.LocalDateTimeSerializer.class)
    private LocalDateTime startTrackDate;
    @JsonSerialize(using = Utils.LocalDateTimeSerializer.class)
    private LocalDateTime finishTrackDate;
    private BigDecimal balanceAtStart;
    private BigDecimal balanceAtEnd;
    private BigDecimal depositDelta;
    private BigDecimal max;
    private BigDecimal min;
    private String outputPath;
    private BigDecimal balanceDifference;
    private int totalTxCount;
    private int totalTradeTxCount;
    private int totalTradeTxCount_2;

    private String user;
    @JsonIgnore
    private List<Transaction> transactions;

    public BalanceReport() {
    }

    public String toPrettyString() {
        final StringBuilder bldr = new StringBuilder("Report for ").append(user).append(":\n");
        return bldr.append("Start date: ").append(startTrackDate).append("\n")
                .append("FinishDate: ").append(finishTrackDate).append("\n")
                .append("Balance at start: ").append(balanceAtStart).append("\n")
                .append("Balance at end: ").append(balanceAtEnd).append("\n")
                .append("Min balance: ").append(min).append("\n")
                .append("Max balance: ").append(max).append("\n")
                .append("Balance Delta: ").append(balanceDifference).append("\n")
                .append("Total transactions: ").append(totalTxCount).append("\n")
                .append("Total trade transactions: ").append(totalTradeTxCount).append("\n")
                .append("Total trade transactions (2): ").append(totalTradeTxCount_2).append("\n")
                .append("Chart: ").append(outputPath).append("\n")
                .toString();
    }

    public String json() {
        try {
            return new ObjectMapper().writer().writeValueAsString(this);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}