package com.example.binanceparser.processor;

import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.sources.InMemorySource;
import com.example.binanceparser.domain.events.AbstractEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InMemoryEventSource extends InMemorySource<AbstractEvent> {
    private final List<Filter> filters;

    public InMemoryEventSource(Stream<AbstractEvent> stream, List<Filter> filters) {
        super(stream);
        this.filters = filters;
    }

    public InMemoryEventSource(Stream<AbstractEvent> stream, Filter... filters) {
        this(stream, List.of(filters));
    }

    @Override
    public List<AbstractEvent> getData() {
        return stream
                .filter(event -> filters.stream().allMatch(filter -> filter.filter(event)))
                .collect(Collectors.toList());
    }
}
