package com.example.binanceparser.util;

import com.example.binanceparser.config.Config;
import com.example.binanceparser.config.IncomeConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class IncomeHistoryVisualizer {


    public static void main(String[] args) throws IOException {
        final IncomeHistoryVisualizer instance = new IncomeHistoryVisualizer();
        Config config = new IncomeConfig();
        config.setInputFilepath("src/main/resources/testJsonLog/log.json");
        instance.run(config);
    }

    public void run(Config config) throws IOException {
        final File logsDir = new File(config.getInputFilepath());
        System.out.println(readBalanceChanges(logsDir));
    }

    public List<Double> readBalanceChanges(File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final String content = reader.lines().collect(Collectors.joining("", "", ""));
        final ObjectMapper mapper = new ObjectMapper();
        final List<IncomeLine> list = mapper.readValue(content, new TypeReference<>() {});
        list.sort(Comparator.comparing(IncomeLine::getTime));
        System.out.println("First date: " + list.get(0).time);
        System.out.println("last date: " + list.get(list.size() - 1).time);
        System.out.println("Absolute profit: " + list.stream().mapToDouble(IncomeLine::getIncome).sum());
        return list.stream().map(IncomeLine::getIncome).collect(Collectors.toList());
    }

    @Data
    static class IncomeLine {
        String symbol;
        String incomeType;
        double income;
        String asset;

        ZonedDateTime time;
        String info;
        long tranId;
        long tradeId;

        public long getTime() {
            return time.toInstant().toEpochMilli();
        }

        public void setTime(long time) {
            this.time = Instant.ofEpochMilli(time).atZone(ZoneId.of("UTC"));
        }
    }
}
