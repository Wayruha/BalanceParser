package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VisualisationRunner {
    private AppProperties appProperties;
    private List<String> users;
    private BalanceVisualizerConfig config;

    public static void main(String[] args) throws IOException {
        VisualisationRunner runner = new VisualisationRunner("src/main/resources/application.properties");
        runner.run();
    }

    public VisualisationRunner(String propertyFile) throws IOException {
        appProperties = ConfigUtil.loadAppProperties(propertyFile);
        users = appProperties.getTrackedPersons();
        config = ConfigUtil.loadVisualizerConfig(appProperties);
    }

    public void run() throws IOException {
        SpotBalanceStateVisualizer spotVisualizer = new SpotBalanceStateVisualizer(appProperties);
        FuturesBalanceStateVisualizer futuresVisualiser = new FuturesBalanceStateVisualizer(appProperties);
        FuturesIncomeVisualizerApp incomeVisualizer = new FuturesIncomeVisualizerApp(appProperties);
        if (users.isEmpty()) {
            users = new CSVEventSource(new File(config.getInputFilepath()), "").getuserIds();
        }
        for (String user : users) {
            BalanceReport spotReport = spotVisualizer.spotBalanceVisualisation(user, config);
            System.out.println("Spot report for " + user + ":");
            System.out.println(spotReport.toPrettyString());

            BalanceReport futuresReport = futuresVisualiser.futuresBalanceVisualisation(user, config);
            System.out.println("Futures report for " + user + ":");
            System.out.println(futuresReport.toPrettyString());

            /*BalanceReport incomeReport = incomeVisualizer.futuresIncomeVisualisation(user, ConfigUtil.loadIncomeConfig(appProperties));
            System.out.println("Income report for " + user + ":");
            System.out.println(incomeReport.toPrettyString());*/
        }
    }
}