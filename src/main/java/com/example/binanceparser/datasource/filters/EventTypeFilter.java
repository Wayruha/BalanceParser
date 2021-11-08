package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.EventType;

public class EventTypeFilter implements Filter{

    private final EventType eventType;

    public EventTypeFilter(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        if(event.getEventType().equals(eventType)) {
            return true;
        };
        //System.out.println(eventType + " is not valid for " + event.getEventType());
        return false;
    }
}
