package com.example.binanceparser;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.BinanceIncomeDataSource;
import com.example.binanceparser.datasource.CSVIncomeModel;
import com.example.binanceparser.datasource.CSVIncomeSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.JsonIncomeSource;
import com.example.binanceparser.processor.IncomeProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.binance.api.client.FuturesIncomeType.REALIZED_PNL;
import static java.util.List.of;

public class FuturesIncomeVisualizerApp {
	private AppProperties appProperties;
	private List<CSVIncomeModel> apiKeysForUsers;

	public FuturesIncomeVisualizerApp(AppProperties appProperties) {
		this.appProperties = appProperties;
		try {
			apiKeysForUsers = new CSVIncomeSource(appProperties.getIncomeInputFilePath()).getIncomeModels();
		} catch (IllegalStateException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/application.properties");
		FuturesIncomeVisualizerApp visualizer = new FuturesIncomeVisualizerApp(appProperties);
		final String trackedPerson = appProperties.getTrackedPersons().get(0);
		final BalanceReport report = visualizer.futuresIncomeVisualisation(trackedPerson, null);
		System.out.println("FuturesIncome report for " + trackedPerson + ":");
		System.out.println(report.toPrettyString());
	}

	public BalanceReport futuresIncomeVisualisation(String user, IncomeConfig _config) {
		final IncomeConfig config = ConfigUtil.loadIncomeConfig(appProperties);
		EventSource<IncomeHistoryItem> apiClientSource = getEventSource(user, config);
		IncomeProcessor processor = new IncomeProcessor(apiClientSource, config);
		return processor.process();
	}

	public EventSource<IncomeHistoryItem> getEventSource(String user, IncomeConfig config) {
		switch (appProperties.getHistoryItemSourceType()) {
		case LOGS:
			return new JsonIncomeSource(new File(config.getInputFilepath()));
		case BINANCE:
			config.setLimit(1000);
			config.setSubject(of(user));
			config.setIncomeType(of(REALIZED_PNL));
			String apiKey = apiKeysForUsers.stream().filter((model) -> model.getUserId().equals(user)).findAny().orElseThrow().getApiKey();
			String secretKey = apiKeysForUsers.stream().filter((model) -> model.getUserId().equals(user)).findAny().orElseThrow().getSecretKey();
			return new BinanceIncomeDataSource(apiKey, secretKey, config);
		default:
			throw new UnsupportedOperationException();
		}
	}
}