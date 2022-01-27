package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.report.BalanceReport;

import java.io.IOException;
import java.util.List;

//TODO треба рефакторити і перейменовувати. я створив це просто для зразку
public class VisualisationRunner {
    private static AppProperties appProperties; 
    private static List<String> users;
    private static BalanceVisualizerConfig config;

    static{
    	try {
			appProperties = ConfigUtil.loadAppProperties("src/main/resources/application.properties");
			users = appProperties.getTrackedPersons();
	        config = ConfigUtil.loadConfig(appProperties);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) throws IOException {
    	SpotBalanceStateVisualizer spotVisualizer = new SpotBalanceStateVisualizer(appProperties);
    	FuturesBalanceStateVisualizer futuresVisualizer = new FuturesBalanceStateVisualizer(appProperties);
        for (String user : users) {
            BalanceReport spotReport = spotVisualizer.spotStateChangeFromLogs(user, config);
            BalanceReport futuresReport = futuresVisualizer.futuresStateChangeFromLogs(user, config);
            System.out.println("Spot report for " + user + ":");
            System.out.println(spotReport.toPrettyString());
            System.out.println("Futures report for " + user + ":");
            System.out.println(futuresReport.toPrettyString());
        }
    }
}