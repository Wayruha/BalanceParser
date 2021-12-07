package com.example.binanceparser.algorithm;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.domain.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class IncomeCalculationAlgorithm {

    public List<IncomeBalanceState> calculateBalance(List<IncomeHistoryItem> incomeList) {
        final List<IncomeBalanceState> incomeBalanceStates = new ArrayList<>();
        BigDecimal cumulativeBalance = new BigDecimal(0);
        for (IncomeHistoryItem income : incomeList) {

            LocalDate dateTime = Instant.ofEpochMilli(income.getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            cumulativeBalance = cumulativeBalance.add(income.getIncome());
            IncomeBalanceState balanceState = new IncomeBalanceState(dateTime, cumulativeBalance, income.getIncomeType());
            incomeBalanceStates.add(balanceState);
        }
        return incomeBalanceStates;
    }

}
