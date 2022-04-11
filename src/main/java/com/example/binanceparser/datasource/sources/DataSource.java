package com.example.binanceparser.datasource.sources;

import java.util.List;
/**
* provides any type deserialization from whichever datasource
*/
public interface DataSource<T> {
    List<T> getData();
}
