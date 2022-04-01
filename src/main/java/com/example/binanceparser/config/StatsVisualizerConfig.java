package com.example.binanceparser.config;

import com.example.binanceparser.datasource.filters.Filter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.MathContext;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsVisualizerConfig extends Config {
    private Set<Filter> filters;
    private int delayPrecision;
    private MathContext relativeContext;
}
