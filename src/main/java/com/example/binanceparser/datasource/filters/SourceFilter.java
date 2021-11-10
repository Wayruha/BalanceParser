package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.events.AbstractEvent;

import java.util.List;

public class SourceFilter implements Filter {

    private final List<String> acceptableNames;

    public SourceFilter(List<String> acceptableNames) {
        this.acceptableNames = acceptableNames;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        return acceptableNames.stream().anyMatch(name -> event.getSource().contains(name));
    }
}
