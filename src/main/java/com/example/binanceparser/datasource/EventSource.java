package com.example.binanceparser.datasource;

import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.domain.events.AbstractEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Provides de-serialized events from whichever datasource (db, logs, csv file, etc.)
 */
public interface EventSource {
    List readEvents(File logsDir, Set<Filter> filters) throws IOException;

}
