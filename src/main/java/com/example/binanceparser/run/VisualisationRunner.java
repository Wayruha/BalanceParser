package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.Utils;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.StatsReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class VisualisationRunner {
    private static final Logger log = Logger.getLogger(VisualisationRunner.class.getName());
    private AppProperties spotBalanceProperties;
    private AppProperties futuresBalanceProperties;
    private AppProperties statsProperties;
    private List<String> spotUsers;
    private List<String> futuresUsers;
    private BalanceVisualizerConfig config;

    public static void main(String[] args) throws IOException {
        configureLogger("src/main/resources/jul-logger.properties");
        VisualisationRunner runner = new VisualisationRunner("src/main/resources/spot-balance.properties", "src/main/resources/futures-balance.properties", "src/main/resources/stats-visualisation.properties");
//        runner.runBalancesVisualisation();
//        runner.runIncomeVisualization();
        runner.runStatsVisualization();
    }

    public VisualisationRunner(String spotBalancePropertyFile, String futuresBalancePropertyFile, String statsVisualisationPropertyFile) throws IOException {
        this.spotBalanceProperties = ConfigUtil.loadAppProperties(spotBalancePropertyFile);
        this.futuresBalanceProperties = ConfigUtil.loadAppProperties(futuresBalancePropertyFile);
        this.statsProperties = ConfigUtil.loadAppProperties(statsVisualisationPropertyFile);
        this.spotUsers = spotBalanceProperties.getTrackedPersons();
        this.futuresUsers = futuresBalanceProperties.getTrackedPersons();
//        this.config = ConfigUtil.loadVisualizerConfig(spotBalanceProperties);
    }

    public void runBalancesVisualisation() throws IOException {
        futuresUsers = spotBalanceProperties.getTrackedPersons();
        config = ConfigUtil.loadVisualizerConfig(spotBalanceProperties);
        if (futuresUsers.isEmpty()) {
            futuresUsers = new CSVEventSource(new File(config.getInputFilepath()), Collections.emptyList()).getUserIds();
        }
        List<BalanceReport> reports = runFuturesVisualization();

        log.info(Utils.toJson(reports));
    }

    private List<BalanceReport> runFuturesVisualization() throws IOException {
        FuturesBalanceStateVisualizer futuresVisualiser = new FuturesBalanceStateVisualizer(futuresBalanceProperties);
        List<BalanceReport> reports = new ArrayList<>();
        for (String user : futuresUsers) {
            log.info("----FUTURES----");
            BalanceReport futuresReport = futuresVisualiser.futuresBalanceVisualisation(user);
            reports.add(futuresReport);
            log.info("Futures report for " + user + ":");
            log.info(futuresReport.toPrettyString());
        }
        return reports;
    }

    private List<BalanceReport> runSpotVisualization() throws IOException {
        SpotBalanceStateVisualizer spotVisualizer = new SpotBalanceStateVisualizer(spotBalanceProperties);
        List<BalanceReport> reports = new ArrayList<>();
        for (String user : spotUsers) {
            log.info("------SPOT------");
            BalanceReport spotReport = spotVisualizer.spotBalanceVisualisation(user);
            log.info("Spot report for " + user + ":");
            log.info(spotReport.toPrettyString());
        }
        return reports;
    }

    public void runIncomeVisualization() {
        futuresUsers = spotBalanceProperties.getTrackedPersons();
        config = ConfigUtil.loadVisualizerConfig(spotBalanceProperties);
        FuturesIncomeVisualizerApp incomeVisualizer = new FuturesIncomeVisualizerApp(spotBalanceProperties);
        if (futuresUsers.isEmpty()) {
            futuresUsers = new ArrayList<>(incomeVisualizer.getUserApiKeys().keySet());
        }

        for (String user : futuresUsers) {
            //we only have one user in csv(RDiachuk)
            BalanceReport incomeReport = incomeVisualizer.futuresIncomeVisualisation(user, ConfigUtil.loadIncomeConfig(spotBalanceProperties));
            log.info("Income report for " + user + ":");
            log.info(incomeReport.toPrettyString());
        }
    }

    public void runStatsVisualization() throws IOException {
        futuresUsers = statsProperties.getTrackedPersons();
        config = ConfigUtil.loadVisualizerConfig(statsProperties);
        StatsVisualizer visualizer = new StatsVisualizer(statsProperties);
        if (futuresUsers.isEmpty()) {
            futuresUsers = new CSVEventSource(new File(config.getInputFilepath()), statsProperties.getTrackedPersons()).getUserIds();
        }

        StatsReport report = visualizer.visualizeStats(futuresUsers);
        log.info(report.toPrettyString());
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