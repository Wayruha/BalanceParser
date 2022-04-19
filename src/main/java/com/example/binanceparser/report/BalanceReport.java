package com.example.binanceparser.report;

import com.example.binanceparser.Utils;
import com.example.binanceparser.datasource.Readable;
import com.example.binanceparser.datasource.Writable;
import com.example.binanceparser.domain.transaction.Transaction;
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
public class BalanceReport implements Writable, Readable {
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
    private String user;
    private String name;
    transient private List<Transaction> transactions;

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
                .append("Chart: ").append(outputPath).append("\n")
                .toString();
    }

    @Override
    public String json() {
        try {
            return new ObjectMapper().writer().writeValueAsString(this);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String csv() {
        StringBuilder csv = new StringBuilder()
                .append(startTrackDate).append(",")
                .append(finishTrackDate).append(",")
                .append(balanceAtStart).append(",")
                .append(balanceAtEnd).append(",")
                .append(depositDelta).append(",")
                .append(max).append(",")
                .append(min).append(",")
                .append(outputPath).append(",")
                .append(balanceDifference).append(",")
                .append(totalTxCount).append(",")
                .append(totalTradeTxCount).append(",")
                .append(user).append(",")
                .append(name).append(System.lineSeparator());
        return csv.toString();
    }

    @Override
    public String header() {
        StringBuilder header = new StringBuilder()
                .append("startTrackDate").append(",")
                .append("finishTrackDate").append(",")
                .append("balanceAtStart").append(",")
                .append("balanceAtEnd").append(",")
                .append("depositDelta").append(",")
                .append("max").append(",")
                .append("min").append(",")
                .append("outputPath").append(",")
                .append("balanceDifference").append(",")
                .append("totalTxCount").append(",")
                .append("totalTradeTxCount").append(",")
                .append("totalTradeTxCount_2").append(",")
                .append("user").append(",")
                .append("name").append(System.lineSeparator());
        return header.toString();
    }

    @Override
    public boolean matches(String header) {
        String[] local = header().split(",");
        String[] headerAsArray = header.split(",");
        if (local.length == headerAsArray.length) {
            for (int i = 0; i < headerAsArray.length; i++) {
                if (!local[i].equalsIgnoreCase(headerAsArray[i])){
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}