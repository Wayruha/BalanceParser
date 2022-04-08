package com.example.binanceparser.report.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class NamePostProcessor extends PostProcessor<AbstractEvent> {
    private static final String DEFAULT_NAME = "unknown";
    private final Map<String, String> usersNames;

    public NamePostProcessor(BalanceVisualizerConfig config) throws FileNotFoundException {
        super(config);
        final File namesFile = new File(config.getNamesFilePath());
        this.usersNames = getUsersNames(namesFile);
    }

    @Override
    public void processReport(BalanceReport report, List<AbstractEvent> events) {
        String name = ofNullable(usersNames.get(report.getUser())).orElse(DEFAULT_NAME);
        report.setName(name);
    }

    private Map<String, String> getUsersNames(File inputFile) throws FileNotFoundException {
        Map<String, String> usersNames = new HashMap<>();
        List<CSVModel> csvPojo = new CsvToBeanBuilder<CSVModel>(new FileReader(inputFile)).withType(CSVModel.class)
                .withSkipLines(1).build().parse();
        csvPojo.stream().forEach((model) -> usersNames.put(model.getUser(), model.getName()));
        return usersNames;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CSVModel {
        @CsvBindByPosition(position = 0)
        private String user;
        @CsvBindByPosition(position = 1)
        private String name;
    }
}
