package com.example.binanceparser.datasource;

import com.example.binanceparser.domain.AbstractEvent;

public interface Filter {
    boolean filter(AbstractEvent event);
}
