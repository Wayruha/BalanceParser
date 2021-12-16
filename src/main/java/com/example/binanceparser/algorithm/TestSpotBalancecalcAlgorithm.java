package com.example.binanceparser.algorithm;

import java.util.List;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.AbstractEvent;

public class TestSpotBalancecalcAlgorithm implements CalculationAlgorithm<SpotIncomeState> {

	private final BalanceVisualizerConfig config;
	
	public TestSpotBalancecalcAlgorithm(BalanceVisualizerConfig config) {
		this.config = config;
	}

	@Override
	public List<SpotIncomeState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {// will contain new implementation
		return null;
	}

	@Override
	public List<SpotIncomeState> processEvents(List<AbstractEvent> events) {
		return processEvents(events, config.getAssetsToTrack());
	}

}
