package com.example.binanceparser.plot;

import com.binance.api.client.domain.account.request.FuturesAccountInfo;
import com.example.binanceparser.Constants;
import com.example.binanceparser.domain.balance.IncomeBalanceState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.math.BigDecimal;
import java.util.List;

public class FuturesRelativeIncomeChartBuilder extends FuturesIncomeChartBuilder {
    private final int imgBalance = 1000;

    @Override
    protected TimeSeriesCollection createTimeSeries(List<IncomeBalanceState> incomeBalanceStates, XYPlot plot) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        final TimeSeries series = new TimeSeries("Як би змінювався баланс, якщо б на початку на ньому було " + imgBalance + " доларів");
        XYLineAndShapeRenderer renderer;

        IncomeStateConverter.convert(incomeBalanceStates, imgBalance);
        incomeBalanceStates.add(0, new IncomeBalanceState(incomeBalanceStates.get(0).getDateTime().minusDays(1), new BigDecimal(imgBalance), null));

        for (IncomeBalanceState incomeBalanceState : incomeBalanceStates) {
            series.addOrUpdate(dateTimeToDay(incomeBalanceState.getDate()),
                    incomeBalanceState.getAvailableBalance());
        }
        dataset.addSeries(series);
        return dataset;
    }

    public static class IncomeStateConverter {
        private static final FuturesAccountInfo info = Constants.BINANCE_CLIENT.getAccountInfo();

        public static void convert(List<IncomeBalanceState> states, int imgBalance) {
            BigDecimal cumulative = new BigDecimal(imgBalance);
            BigDecimal startingBalance = info.getTotalWalletBalance().subtract(states.get(states.size() - 1).getAvailableBalance());
            BigDecimal prevBalance = startingBalance;
            for (IncomeBalanceState currState : states) {
                BigDecimal income = startingBalance.add(currState.getAvailableBalance()).subtract(prevBalance);
                cumulative = cumulative.add(income.divide(prevBalance, Constants.MATH_CONTEXT).multiply(cumulative, Constants.MATH_CONTEXT));
                currState.setAvailableBalance(cumulative);
                prevBalance = prevBalance.add(income);
            }
        }
    }
}