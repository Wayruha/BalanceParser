package com.example.binanceparser.report;

import lombok.Value;

import java.util.Comparator;
import java.util.List;

@Value
public class Statistics<T extends Number & Comparable > {
    int sampleCount;
    T sum;
    T max;
    T min;
    double average;
    T _75percentile;
    T _90percentile;

    public Statistics(List<T> data) {
        data.sort(Comparator.naturalOrder());
        this.min = data.get(0);
        this.max = data.get(data.size() - 1);
        this.sampleCount = data.size();
        this.sum = (T) Double.valueOf(data.stream().mapToDouble(Number::doubleValue).sum());
        this.average = this.sum.doubleValue() / this.sampleCount;
        this._75percentile = calcPercentile(data, 75);
        this._90percentile = calcPercentile(data, 90);

    }

    /**
     * CAREFUL: method references fields of this instance which MAY NOT be initialized
     * @param percentile of data which is under certain threshold
     */
    private T calcPercentile(List<T> sortedData, double percentile) {
        double thresholdSum = percentile / 100 * this.sum.doubleValue();
        double runningSum = 0;
        for (T n : sortedData) {
            runningSum += n.doubleValue();
            if (runningSum >= thresholdSum) return n;
        }

        throw new IllegalStateException("Should never reach it.");
    }
}
