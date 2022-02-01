package com.example.binanceparser;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.BinanceIncomeDataSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.JsonIncomeSource;
import com.example.binanceparser.processor.IncomeProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import static com.binance.api.client.FuturesIncomeType.REALIZED_PNL;
import static java.util.List.of;

public class FuturesIncomeVisualizerApp {
	private AppProperties appProperties;

	public FuturesIncomeVisualizerApp(AppProperties appProperties) {
		this.appProperties = appProperties;
	}
	
	public BalanceReport futuresIncomeVisualisation(String user, IncomeConfig config) {
		EventSource<IncomeHistoryItem> apiClientSource = getEventSource(user, config);
		IncomeProcessor processor = new IncomeProcessor(apiClientSource, config);
		return processor.process();
	}
	
	public EventSource<IncomeHistoryItem> getEventSource(String user, IncomeConfig config){
		switch(appProperties.getHistoryItemSourceType()) {
		case LOGS:
			return new JsonIncomeSource(new File(config.getInputFilepath()));
		case BINANCE: 
			config.setLimit(1000);
			//config.setSubject(of(user));
			config.setSubject(of("RDiachuk"));
			config.setIncomeType(of(REALIZED_PNL));
			return new BinanceIncomeDataSource(Constants.BINANCE_API_KEY,
					Constants.BINANCE_SECRET_KEY, config);
		default:
			throw new UnsupportedOperationException();
		}
	}
}