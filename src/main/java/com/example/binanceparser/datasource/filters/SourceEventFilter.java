package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.AbstractEvent;

public class SourceEventFilter implements Filter{

    private final String acceptableName;

    public SourceEventFilter(String acceptableName) {
        this.acceptableName = acceptableName;
    }

    @Override
    public boolean filter(AbstractEvent event) {

        if(event.getSource().equals(acceptableName)) {
            return true;
        };
        //System.out.println(event.getSource() + " is not valid for " + acceptableName);
        return false;
    }
}
