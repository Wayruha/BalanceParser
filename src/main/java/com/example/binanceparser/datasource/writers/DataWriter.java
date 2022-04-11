package com.example.binanceparser.datasource.writers;

import java.util.List;

//provides any serialization/writing to db etc.
public interface DataWriter<T> {
    void writeEvents(List<T> items);
}
