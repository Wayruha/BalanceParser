package com.example.binanceparser;

import com.example.binanceparser.algorithm.FuturesWalletBalanceCalcAlgorithm;
import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

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
        	Logger.getLogger(SpotBalanceCalcAlgorithm.class.getName()).setLevel(appProperties.getLoggerLevel());
        	Logger.getLogger(SpotBalanceProcessor.class.getName()).setLevel(appProperties.getLoggerLevel());
            BalanceReport spotReport = spotVisualizer.spotBalanceVisualisation(user, config);
            System.out.println("Spot report for " + user + ":");
            System.out.println(spotReport.toPrettyString());

            Logger.getLogger(FuturesWalletBalanceCalcAlgorithm.class.getName()).setLevel(appProperties.getLoggerLevel());
            Logger.getLogger(FuturesBalanceStateProcessor.class.getName()).setLevel(appProperties.getLoggerLevel());
            BalanceReport futuresReport = futuresVisualiser.futuresBalanceVisualisation(user, config);
            System.out.println("Futures report for " + user + ":");
            System.out.println(futuresReport.toPrettyString());
//we only have one user (RDiachuk)
//            BalanceReport incomeReport = incomeVisualizer.futuresIncomeVisualisation(user, ConfigUtil.loadIncomeConfig(appProperties));
//            System.out.println("Income report for " + user + ":");
//            System.out.println(incomeReport.toPrettyString());
        }
    }
}