package com.example.binanceparser.datasource;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface Writable {
    default String json() {
        try {
            return new ObjectMapper().writer().writeValueAsString(this);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    };
    String csv();
}
