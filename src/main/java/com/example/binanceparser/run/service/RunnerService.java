package com.example.binanceparser.run.service;

import com.example.binanceparser.report.AggregatedBalanceReport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RunnerService {
    AggregatedBalanceReport getReport(List<String> trackedPersons);
}
