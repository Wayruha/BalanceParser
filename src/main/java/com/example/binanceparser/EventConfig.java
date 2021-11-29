package com.example.binanceparser;

import com.example.binanceparser.domain.events.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventConfig extends Config {
    List<String> sourceToTrack;
    List<String> assetsToTrack;
    boolean convertToUSD;
    List<EventType> eventType;
}
