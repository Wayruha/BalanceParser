package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.report.BalanceReport;

import java.io.IOException;
import java.util.List;

//TODO треба рефакторити і перейменовувати. я створив це просто для зразку
public class ManyUsersRun {
    final AppProperties appProperties;
    final List<String> users;
    final BalanceVisualizerConfig config;

    public ManyUsersRun(List<String> users) throws IOException {
        this.appProperties = ConfigUtil.loadAppProperties("src/main/resources/application.properties");
        this.users = List.of(appProperties.getTrackedPerson().split(","));
        this.config = ConfigUtil.loadConfig(appProperties);
    }

    //TODO зробити точку входу- static main
    public void run() throws IOException {
        BalanceStateVisualizer spotVisualizer = new BalanceStateVisualizer(appProperties);
        for (String user : users) {
            BalanceReport report = spotVisualizer.spotStateChangeFromLogs(user, config);
            // todo generate futures report too
            System.out.println("Test report for " + user + ":");
            System.out.println(report.toPrettyString());
        }
    }

}
