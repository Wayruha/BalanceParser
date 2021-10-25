package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;

import java.io.IOException;
import java.util.List;

/**
 * Provides de-serialized events from whichever datasource (db, logs, csv file, etc.)
 */
public interface EventSource {
    List<AbstractEvent> readEvents(String path) throws IOException;
}
