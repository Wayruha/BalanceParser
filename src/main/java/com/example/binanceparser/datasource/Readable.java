package com.example.binanceparser.datasource;

public interface Readable {
    String header();
    default boolean matches(String header) {
        String[] local = header().split(",");
        String[] headerAsArray = header.split(",");
        if (local.length == headerAsArray.length) {
            for (int i = 0; i < headerAsArray.length; i++) {
                if (!local[i].equalsIgnoreCase(headerAsArray[i])){
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}