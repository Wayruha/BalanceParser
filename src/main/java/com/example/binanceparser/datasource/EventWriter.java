package com.example.binanceparser.datasource;

import java.util.List;

import com.example.binanceparser.domain.events.AbstractEvent;

//provides event serialization/writing to db etc.
public interface EventWriter<T extends AbstractEvent> {
	public void writeEvents(List<T> events);
}
