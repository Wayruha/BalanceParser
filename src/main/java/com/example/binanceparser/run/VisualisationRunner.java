package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.Utils;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.CSVEventSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.statistics.StatsReport;
import com.example.binanceparser.statistics.StatsVisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// todo completely remove this entry-point
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
        runner.runVisualisation();
//        runner.runIncomeVisualization();
//        runner.runStatsVisualization();
    }

    public VisualisationRunner(String spotBalancePropertyFile, String futuresBalancePropertyFile, String statsVisualisationPropertyFile) throws IOException {
        this.spotBalanceProperties = ConfigUtil.loadAppProperties(spotBalancePropertyFile);
        this.futuresBalanceProperties = ConfigUtil.loadAppProperties(futuresBalancePropertyFile);
        this.statsProperties = ConfigUtil.loadAppProperties(statsVisualisationPropertyFile);
        this.spotUsers = spotBalanceProperties.getTrackedPersons();
        this.futuresUsers = futuresBalanceProperties.getTrackedPersons();
    }

    public void runVisualisation() throws IOException {
        config = ConfigUtil.loadVisualizerConfig(futuresBalanceProperties);
        if (futuresUsers.isEmpty()) {
            final CSVEventSource redundantEventSource = new CSVEventSource(new File(config.getInputFilepath()), Collections.emptyList());
            futuresUsers = extractAllUsersIds(redundantEventSource);
        }
        final List<BalanceReport> reports = runFuturesVisualization(futuresUsers);
        log.info(Utils.toJson(reports));
    }

    //TODO don't use it. remove
    private List<BalanceReport> runFuturesVisualization(List<String> users) throws IOException {
        final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(futuresBalanceProperties);
        final DataSource<AbstractEvent> eventSource = Helper.getEventSource(futuresBalanceProperties.getDataSourceType(), config);
        final DataSource<UserNameRel> nameSource = Helper.getNameSource(futuresBalanceProperties.getDataSourceType(), config);
        FuturesBalanceStateVisualizer futuresVisualiser = new FuturesBalanceStateVisualizer(futuresBalanceProperties, config, eventSource, nameSource);
        List<BalanceReport> reports = new ArrayList<>();
        for (String user : users) {
            log.info("----FUTURES----");
            BalanceReport futuresReport = futuresVisualiser.singleUserVisualization(user);
            reports.add(futuresReport);
            log.info("Futures report for " + user + ":");
            log.info(futuresReport.toPrettyString());
        }
        return reports;
    }

    private List<String> extractAllUsersIds(CSVEventSource redundantEventSource) {
        return redundantEventSource.getData().stream()
                .map(AbstractEvent::getSource)
                .distinct()
                .collect(Collectors.toList());
    }

    public void runIncomeVisualization() throws Exception {
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

        List<StatsReport> reports = visualizer.calculateStatistics(futuresUsers);
        reports.forEach(report -> {
            log.info("Report: " + report.getType());
            log.info(report.toString());
        });
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