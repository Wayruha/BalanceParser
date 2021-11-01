package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides de-serialized events from whichever datasource (db, logs, csv file, etc.)
 */
public interface EventSource {
    List<AbstractEvent> readEvents(File logsDir, LocalDateTime startTrackBalance, LocalDateTime finishTrackBalance) throws IOException;
}
