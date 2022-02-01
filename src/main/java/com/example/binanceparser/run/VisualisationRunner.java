package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class VisualisationRunner {
    private static final Logger log = Logger.getLogger(VisualisationRunner.class.getName());
    private AppProperties appProperties;
    private List<String> users;
    private BalanceVisualizerConfig config;

    public static void main(String[] args) throws IOException {
        configureLogger("src/main/resources/jul-logger.properties");
        VisualisationRunner runner = new VisualisationRunner("src/main/resources/application.properties");
        runner.runBalancesVisualisation();
//        runner.runIncomeVisualization();
    }

    public VisualisationRunner(String propertyFile) throws IOException {
        this.appProperties = ConfigUtil.loadAppProperties(propertyFile);
        this.users = appProperties.getTrackedPersons();
        this.config = ConfigUtil.loadVisualizerConfig(appProperties);
    }

    public void runBalancesVisualisation() throws IOException {
        SpotBalanceStateVisualizer spotVisualizer = new SpotBalanceStateVisualizer(appProperties);
        FuturesBalanceStateVisualizer futuresVisualiser = new FuturesBalanceStateVisualizer(appProperties);
        if (users.isEmpty()) {
            users = new CSVEventSource(new File(config.getInputFilepath()), "").getuserIds();
        }
        List<BalanceReport> reports = new ArrayList<>();
        for (String user : users) {
            /*log.info("------SPOT------");
            BalanceReport spotReport = spotVisualizer.spotBalanceVisualisation(user, config);
            log.info("Spot report for " + user + ":");
            log.info(spotReport.toPrettyString());
*/
            log.info("----FUTURES----");
            BalanceReport futuresReport = futuresVisualiser.futuresBalanceVisualisation(user, config);
            reports.add(futuresReport);
            log.info("Futures report for " + user + ":");
            log.info(futuresReport.toPrettyString());
        }
        reports.forEach(r -> log.info(r.json()));
    }

    public void runIncomeVisualization() {
        FuturesIncomeVisualizerApp incomeVisualizer = new FuturesIncomeVisualizerApp(appProperties);
        if (users.isEmpty()) {
            users = new ArrayList<>(incomeVisualizer.getUserApiKeys().keySet());
        }

        for (String user : users) {
            //we only have one user in csv(RDiachuk)
            BalanceReport incomeReport = incomeVisualizer.futuresIncomeVisualisation(user, ConfigUtil.loadIncomeConfig(appProperties));
            log.info("Income report for " + user + ":");
            log.info(incomeReport.toPrettyString());
        }
    }

    public static void configureLogger(String loggingConfig) {
        try {
            FileInputStream fis = new FileInputStream(loggingConfig);
            LogManager.getLogManager().readConfiguration(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}