package com.example.binanceparser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.events.AbstractEvent;

public abstract class BalanceStateVisualizer {
	protected static Set<Filter> filters(BalanceVisualizerConfig config) {
		final Set<Filter> filters = new HashSet<>();
		if (config.getSubject() != null) {
			filters.add(new SourceFilter(config.getSubject()));
		}

		if (config.getEventType() != null) {
			filters.add(new EventTypeFilter(config.getEventType()));
		}
		return filters;
	}

	protected EventSource<AbstractEvent> getEventSource(AppProperties.DatasourceType datasourceType, BalanceVisualizerConfig config) {
		final File logsDir = new File(config.getInputFilepath());
		EventSource<AbstractEvent> eventSource;
		switch (datasourceType) {
			case CSV:
				eventSource = new CSVEventSource(logsDir, config.getSubject().get(0));
				break;
			case LOGS:
				eventSource = new LogsEventSource(logsDir, filters(config));
				break;
			default:
				throw new RuntimeException("unknown event source type specified");
		}
		return eventSource;
	}
}
