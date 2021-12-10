package com.example.binanceparser.algorithm;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class IncomeCalculationAlgorithm {

    public List<IncomeBalanceState> calculateBalance(List<IncomeHistoryItem> incomeList) {
        final List<IncomeBalanceState> incomeBalanceStates = new ArrayList<>();
        BigDecimal cumulativeBalance = new BigDecimal(0);
        for (IncomeHistoryItem income : incomeList) {

            LocalDateTime dateTime = Instant.ofEpochMilli(income.getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            cumulativeBalance = cumulativeBalance.add(income.getIncome());
            IncomeBalanceState balanceState = new IncomeBalanceState(dateTime, cumulativeBalance, income.getIncomeType());
            incomeBalanceStates.add(balanceState);
        }
        return incomeBalanceStates;
    }

}
