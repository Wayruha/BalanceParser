package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.events.AbstractEvent;

import java.time.LocalDateTime;

public class DateEventFilter implements Filter{

    private final LocalDateTime startTrackDate;
    private final LocalDateTime finishTrackDate;

    public DateEventFilter(LocalDateTime startTrackDate, LocalDateTime finishTrackDate) {
        this.startTrackDate = startTrackDate;
        this.finishTrackDate = finishTrackDate;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        return (startTrackDate == null ||event.getDateTime().isAfter(startTrackDate)) &&
                (finishTrackDate == null ||event.getDateTime().isBefore(finishTrackDate));
    }
}
