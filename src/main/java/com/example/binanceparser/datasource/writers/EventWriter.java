package com.example.binanceparser.datasource.writers;

import java.util.List;

import com.example.binanceparser.domain.events.AbstractEvent;

//provides event serialization/writing to db etc.
public interface EventWriter<T extends AbstractEvent> extends DataWriter<T> {
	@Override
	void writeEvents(List<T> events);
}
