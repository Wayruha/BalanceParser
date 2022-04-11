package com.example.binanceparser.datasource.sources;

import com.example.binanceparser.domain.events.AbstractEvent;

import java.util.List;

//TODO думаю нам не треба цей інтерфейс. ми можемо його повністю видалити, і юзати на його місці DataSource<AbstractEvent>
/**
 * Provides de-serialized events from whichever datasource (db, logs, csv file, etc.)
 */
public interface EventSource<T extends AbstractEvent> extends DataSource<T> {
    @Override
    List<T> getData();
}
