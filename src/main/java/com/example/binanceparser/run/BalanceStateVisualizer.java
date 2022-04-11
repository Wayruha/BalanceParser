package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.*;
import com.example.binanceparser.domain.events.AbstractEvent;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

	public static DataSource<AbstractEvent> getEventSource(AppProperties.DatasourceType datasourceType, BalanceVisualizerConfig config) {
		final File logsDir = new File(config.getInputFilepath());
		DataSource<AbstractEvent> eventSource;
		switch (datasourceType) {
			case CSV:
				eventSource = new CSVEventSource(logsDir, config.getSubject());
				break;
			case LOGS:
				eventSource = new LogsEventSource(logsDir, filters(config));
				break;
			default:
				throw new RuntimeException("unknown event source type specified");
		}
		return eventSource;
	}

	public static DataSource<UserNameRel> getNameSource(AppProperties.DatasourceType datasourceType, BalanceVisualizerConfig config) {
		final File file = new File(config.getNamesFilePath());
		DataSource<UserNameRel> nameSource;
		switch (datasourceType) {
			case CSV:
				nameSource = new CSVDataSource<>(file, 1, UserNameRel.class);
				break;
			case LOGS:
				throw new NotImplementedException("nameSource is not yet implemented for logs");
			default:
				throw new RuntimeException("unknown event source type specified");
		}
		return nameSource;
	}
}
