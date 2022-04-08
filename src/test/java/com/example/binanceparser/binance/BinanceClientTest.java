package com.example.binanceparser.binance;

import com.binance.api.client.FuturesIncomeType;
import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.Constants;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

class BinanceClientTest {

    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testIncomeHistory() {
        BinanceClient binanceClient = new BinanceClient(Constants.BINANCE_API_KEY, Constants.BINANCE_SECRET_KEY);
        Instant start = LocalDateTime.parse("2021-01-01 13:15:50", dateFormat).atZone(ZoneId.of("Europe/Paris")).toInstant();
        Instant end = LocalDateTime.parse("2021-11-30 13:15:50", dateFormat).atZone(ZoneId.of("Europe/Paris")).toInstant();
        List<IncomeHistoryItem> items = binanceClient.fetchFuturesIncomeHistory(null, FuturesIncomeType.TRANSFER, start, end, 1000);
        List<LocalDate> localDates = items.stream()
                .map(income -> Instant.ofEpochMilli(income.getTime()).atZone(ZoneId.systemDefault()).toLocalDate()).collect(Collectors.toList());
        localDates.forEach(System.out::println);
    }
}