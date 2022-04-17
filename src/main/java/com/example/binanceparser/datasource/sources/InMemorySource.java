package com.example.binanceparser.datasource.sources;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemorySource<T> implements DataSource<T> {
    protected final Stream<T> stream;

    public InMemorySource(Stream<T> stream) {
        this.stream = stream;
    }

    @Override
    public List<T> getData() {
        return stream.collect(Collectors.toList());
    }
}
