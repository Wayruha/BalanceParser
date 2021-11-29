package com.example.binanceparser.datasource;

import java.util.List;

/**
 * Provides de-serialized events from whichever datasource (db, logs, csv file, etc.)
 */
public interface EventSource<T> {
    List<T> getData();
}
