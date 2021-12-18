package com.example.binanceparser.report;

import java.io.IOException;
import java.util.List;
import com.example.binanceparser.config.Config;
import com.example.binanceparser.domain.BalanceState;

public abstract class AbstractBalanceReportGenerator<T extends BalanceState, V extends Config> {

	protected V config;
	
	public AbstractBalanceReportGenerator(V config) {
		this.config = config;
	}
	
	public abstract BalanceReport getBalanceReport(List<T> balanceStates) throws IOException;
	
}
