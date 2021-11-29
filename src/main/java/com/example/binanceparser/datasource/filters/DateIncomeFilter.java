package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.Income;
import com.example.binanceparser.domain.events.AbstractEvent;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;

@AllArgsConstructor
public class DateIncomeFilter {

    private final LocalDateTime startTrackDate;
    private final LocalDateTime finishTrackDate;

    public boolean filter(Income income) {
        LocalDate incomeDate = Instant.ofEpochMilli(income.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return (startTrackDate == null || incomeDate.isAfter(ChronoLocalDate.from(startTrackDate))) &&
                (finishTrackDate == null || incomeDate.isBefore(ChronoLocalDate.from(finishTrackDate)));
    }
}
