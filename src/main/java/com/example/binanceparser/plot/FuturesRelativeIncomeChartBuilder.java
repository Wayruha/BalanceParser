package com.example.binanceparser.plot;

import com.binance.api.client.domain.account.request.FuturesAccountInfo;
import com.example.binanceparser.Constants;
import com.example.binanceparser.domain.balance.IncomeBalanceState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FuturesRelativeIncomeChartBuilder extends FuturesIncomeChartBuilder {
    @Override
    protected TimeSeriesCollection createTimeSeries(List<IncomeBalanceState> incomeBalanceStates, XYPlot plot) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        final TimeSeries series = new TimeSeries("Profit %");
        XYLineAndShapeRenderer renderer;

        List<IncomeBalanceState> converted = IncomeStateConverter.convert(incomeBalanceStates);

        for (IncomeBalanceState incomeBalanceState : converted) {
            series.addOrUpdate(dateTimeToDay(incomeBalanceState.getDate()),
                    incomeBalanceState.getAvailableBalance());
        }
        dataset.addSeries(series);
        return dataset;
    }

    private static class IncomeStateConverter {
        private static final FuturesAccountInfo info = Constants.BINANCE_CLIENT.getAccountInfo();

        public static List<IncomeBalanceState> convert(List<IncomeBalanceState> states) {
            List<IncomeBalanceState> buffer = new ArrayList<>(states);
            BigDecimal cumulative = new BigDecimal("100");
            BigDecimal startingBalance = info.getTotalWalletBalance().subtract(buffer.get(buffer.size() - 1).getAvailableBalance());
            BigDecimal prevBalance = startingBalance;
            for (IncomeBalanceState currState : buffer) {
                BigDecimal income = startingBalance.add(currState.getAvailableBalance()).subtract(prevBalance);
                cumulative = cumulative.add(income.divide(prevBalance, Constants.MATH_CONTEXT).multiply(new BigDecimal("100")));
                currState.setAvailableBalance(cumulative);
                prevBalance = prevBalance.add(income);
            }
            return buffer;
        }
    }
}