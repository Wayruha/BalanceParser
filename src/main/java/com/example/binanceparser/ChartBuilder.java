package com.example.binanceparser;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;

// it can be named a 'LineMoneyChart'
public class ChartBuilder {

    public JFreeChart buildLineChart(List<BalanceState> balanceStates){
        final DefaultCategoryDataset dataset = createDataset(balanceStates);

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Account balance", "Years", "Wallet", dataset, PlotOrientation.VERTICAL, true, true, false
        );
       return lineChart;
    }


    private DefaultCategoryDataset createDataset(List<BalanceState> balanceStates)  {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        //TODO use data from balanceStates here to create a dataset,
        for(BalanceState balanceState: balanceStates) {

            dataset.addValue(balanceState.getAssets().get(0).getAvailableBalance(), "Money", balanceState.getDateTime());
        }
       return dataset;
    }
}