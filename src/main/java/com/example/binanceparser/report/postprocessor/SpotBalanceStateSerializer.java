package com.example.binanceparser.report.postprocessor;

import com.example.binanceparser.Constants;
import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.datasource.Readable;
import com.example.binanceparser.datasource.Writable;
import com.example.binanceparser.datasource.writers.CSVDataWriter;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
public class SpotBalanceStateSerializer extends PostProcessor<AbstractEvent, BalanceReport> {
    private DataWriter<SpotBalancePoint> serializer;
    private String asset = Constants.VIRTUAL_USD;

    public SpotBalanceStateSerializer(OutputStream outputStream, String asset) {
        serializer = new CSVDataWriter<>(outputStream, SpotBalancePoint.class);
        this.asset = asset;
    }

    public SpotBalanceStateSerializer(OutputStream outputStream) {
        serializer = new CSVDataWriter<>(outputStream, SpotBalancePoint.class);
        this.asset = Constants.VIRTUAL_USD;
    }

    @Override
    public void processReport(BalanceReport balanceReport, List<AbstractEvent> incomes) {
        List<SpotBalancePoint> spotBalanceStates = new SpotBalanceCalcAlgorithm().processEvents(incomes).stream()
                .map(bs -> new SpotBalancePoint(bs.getDateTime(), bs.calculateVirtualUSDBalance(asset))).collect(Collectors.toList());
        serializer.write(spotBalanceStates);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class SpotBalancePoint implements Writable, Readable {
        private LocalDateTime date;
        private BigDecimal balance;

        @Override
        public String csv() {
            StringBuilder csv = new StringBuilder()
                    .append(date).append(",")
                    .append(balance).append(System.lineSeparator());
            return csv.toString();
        }

        @Override
        public String header() {
            StringBuilder header = new StringBuilder()
                    .append("date").append(",")
                    .append("balance").append(System.lineSeparator());
            return header.toString();
        }
    }
}
