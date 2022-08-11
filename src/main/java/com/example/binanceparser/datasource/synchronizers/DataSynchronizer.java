package com.example.binanceparser.datasource.synchronizers;

import java.io.FileNotFoundException;

public interface DataSynchronizer<DataType> {
    void synchronize() throws FileNotFoundException;
}
