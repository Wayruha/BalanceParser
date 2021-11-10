package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.events.AbstractEvent;

public interface Filter {
    boolean filter(AbstractEvent event);
}
