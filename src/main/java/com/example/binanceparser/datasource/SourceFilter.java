package com.example.binanceparser.datasource;

import com.example.binanceparser.domain.AbstractEvent;

public class SourceFilter implements Filter {
    private final String acceptableName;

    public SourceFilter(String acceptableName) {
        this.acceptableName = acceptableName;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        return event.getSource().equals(acceptableName);
    }
}
