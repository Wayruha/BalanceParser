package com.example.binanceparser;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;

public class ChartBuilder {

    public JFreeChart buildLineChart(List<BalanceState> balanceStates, String assetToTrack){
        final DefaultCategoryDataset dataset = createDataset(balanceStates, assetToTrack);

        return ChartFactory.createLineChart(
                "Account balance", "Date", "Balance", dataset, PlotOrientation.VERTICAL, true, true, false
        );
    }

    private DefaultCategoryDataset createDataset(List<BalanceState> balanceStates, String assetToTrack)  {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(BalanceState balanceState: balanceStates) {
            final BalanceState.Asset asset = balanceState.getAssets().stream().
                    filter(a -> a.getAsset().equals(assetToTrack)).findFirst().get();
            dataset.addValue(asset.getAvailableBalance(), asset.getAsset(), balanceState.getDateTime());
        }
       return dataset;
    }
}