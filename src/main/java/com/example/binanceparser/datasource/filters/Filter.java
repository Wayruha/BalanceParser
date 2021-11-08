package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.AbstractEvent;

public interface Filter {
    boolean filter(AbstractEvent event);
}
