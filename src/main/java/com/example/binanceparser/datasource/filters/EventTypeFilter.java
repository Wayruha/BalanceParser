package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;

import java.util.List;

public class EventTypeFilter implements Filter{

    private final List<EventType> eventTypes;

    public EventTypeFilter(List<EventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        return eventTypes.stream().anyMatch(eventType -> event.getEventType().equals(eventType));
    }
}
