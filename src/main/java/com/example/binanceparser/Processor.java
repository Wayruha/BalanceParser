package com.example.binanceparser;

import com.example.binanceparser.config.Config;
import com.example.binanceparser.report.BalanceReport;

import java.io.IOException;

public interface Processor {

    BalanceReport run(Config config) throws IOException;
}
