package com.example.binanceparser.config;

import com.example.binanceparser.datasource.filters.Filter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.MathContext;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsVisualizerConfig extends Config {
    private List<Filter> filters;
    private int delayPrecision;
    private MathContext relativeContext;
}
