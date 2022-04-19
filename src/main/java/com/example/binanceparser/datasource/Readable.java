package com.example.binanceparser.datasource;

public interface Readable {
    String header();
    boolean matches(String header);
}
