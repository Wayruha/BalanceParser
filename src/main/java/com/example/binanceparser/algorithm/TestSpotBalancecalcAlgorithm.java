package com.example.binanceparser.algorithm;

import java.util.List;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;

public class TestSpotBalancecalcAlgorithm extends SpotBalanceCalcAlgorithm{

	public TestSpotBalancecalcAlgorithm(BalanceVisualizerConfig config) {
		super(config);
	}
	
	@Override
	public List<EventBalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {//will contain new implementation
		return null;
	}

}
