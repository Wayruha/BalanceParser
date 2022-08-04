package com.example.binanceparser.report.postprocessor;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.balance.IncomeBalanceState;
import com.example.binanceparser.plot.FuturesRelativeIncomeChartBuilder;
import com.example.binanceparser.report.BalanceReport;

import java.math.BigDecimal;
import java.util.List;

public class IncomeHistorySerializer extends PostProcessor<IncomeHistoryItem, BalanceReport> {
    private DataWriter<IncomeBalanceState> serializer;
    private int imgBalance = 1000;

    public IncomeHistorySerializer(DataWriter<IncomeBalanceState> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void processReport(BalanceReport balanceReport, List<IncomeHistoryItem> incomes) {
        List<IncomeBalanceState> incomeBalanceStates = new IncomeCalculationAlgorithm().calculateBalance(incomes);
        FuturesRelativeIncomeChartBuilder.IncomeStateConverter.convert(incomeBalanceStates, imgBalance);
        incomeBalanceStates.add(0, new IncomeBalanceState(incomeBalanceStates.get(0).getDateTime().minusDays(1), new BigDecimal(imgBalance), null));
        serializer.write(incomeBalanceStates);
    }
}
