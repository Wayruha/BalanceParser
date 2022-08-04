package com.example.binanceparser.domain.balance;

import com.binance.api.client.FuturesIncomeType;
import com.example.binanceparser.datasource.Readable;
import com.example.binanceparser.datasource.Writable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class IncomeBalanceState extends BalanceState implements Writable, Readable {
    private FuturesIncomeType incomeType;
    private BigDecimal availableBalance;
    
    public IncomeBalanceState(LocalDateTime dateTime, BigDecimal availableBalance, FuturesIncomeType incomeType) {
        super(dateTime);
        this.availableBalance = availableBalance;
        this.incomeType = incomeType;
    }

    @Override
    public String csv() {
        StringBuilder csv = new StringBuilder()
                .append(incomeType).append(",")
                .append(availableBalance).append(System.lineSeparator());
        return csv.toString();
    }

    @Override
    public String header() {
        StringBuilder header = new StringBuilder()
                .append("incomeType").append(",")
                .append("availableBalance").append(System.lineSeparator());
        return header.toString();
    }

}
