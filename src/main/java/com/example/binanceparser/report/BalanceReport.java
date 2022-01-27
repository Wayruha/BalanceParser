package com.example.binanceparser.report;

import com.example.binanceparser.domain.Transaction;
import com.example.binanceparser.domain.TransactionX;
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

    private String user; //TODO юзер, або налл якщо не вказано
    //TODO проставити всі транзакції які відбулися для цього юзера в заданому проміжку
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
                .append("Total transactions:").append(transactions.size()).append("\n")
                .append("Chart: ").append(outputPath).append("\n")
                .toString();
    }
}
