package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class VisualisationRunner {
    private static final Logger log = Logger.getLogger(VisualisationRunner.class.getName());
    private AppProperties appProperties;
    private List<String> users;
    private BalanceVisualizerConfig config;

    public static void main(String[] args) throws IOException {
        configureLogger( "src/main/resources/jul-logger.properties");
        VisualisationRunner runner = new VisualisationRunner("src/main/resources/application.properties");
        runner.run();
    }

    public VisualisationRunner(String propertyFile) throws IOException {
        this.appProperties = ConfigUtil.loadAppProperties(propertyFile);
        this.users = appProperties.getTrackedPersons();
        this.config = ConfigUtil.loadVisualizerConfig(appProperties);
    }

    public void run() throws IOException {
        SpotBalanceStateVisualizer spotVisualizer = new SpotBalanceStateVisualizer(appProperties);
        FuturesBalanceStateVisualizer futuresVisualiser = new FuturesBalanceStateVisualizer(appProperties);
        FuturesIncomeVisualizerApp incomeVisualizer = new FuturesIncomeVisualizerApp(appProperties);
        if (users.isEmpty()) {
            users = new CSVEventSource(new File(config.getInputFilepath()), "").getuserIds();
        }
        for (String user : users) {
/*            BalanceReport spotReport = spotVisualizer.spotBalanceVisualisation(user, config);
            log.info("Spot report for " + user + ":");
            log.info(spotReport.toPrettyString());*/

            BalanceReport futuresReport = futuresVisualiser.futuresBalanceVisualisation(user, config);
            log.info("Futures report for " + user + ":");
            log.info(futuresReport.toPrettyString());

            /*BalanceReport incomeReport = incomeVisualizer.futuresIncomeVisualisation(user, ConfigUtil.loadIncomeConfig(appProperties));
            log.info("Income report for " + user + ":");
            log.info(incomeReport.toPrettyString());*/
        }
    }

    public static void configureLogger(String loggingConfig){
        try {
            FileInputStream fis = new FileInputStream(loggingConfig);
            LogManager.getLogManager().readConfiguration(fis);
            fis.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}