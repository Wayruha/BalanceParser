package com.example.binanceparser;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.EventType;
import com.example.binanceparser.domain.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.FuturesOrderTradeUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.binanceparser.Filter.fromPlainToJson;
import static com.example.binanceparser.domain.EventType.*;

/**
 * read directory with logs to provide the events
 */
public class LogsEventSource implements EventSource {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<AbstractEvent> readEvents(File srcDir) throws IOException {
        String[] contents = srcDir.list();
        Arrays.sort(contents);
        List<AbstractEvent> allEvents = new ArrayList<>();
        for (String filePath : contents) {
            File file = new File(srcDir.getAbsolutePath() + "/" + filePath);
            Document doc = Jsoup.parse(file, "UTF-8");
            List<Element> messageList = doc.getElementsByClass("info");
//            messageList.remove(0); // remove first element of logs table (bc it`s table header)
            for (Element element : messageList) {
                LocalDateTime date = LocalDateTime.parse(element.getElementsByClass("Date").text(), dateFormat);
                final String logLine = element.getElementsByClass("Message").text();
                final AbstractEvent event = parseLogLine(date, logLine);
                if (event != null) allEvents.add(event);
            }
        }
        return allEvents;
    }

    public AbstractEvent parseLogLine(LocalDateTime date, String logLine) throws JsonProcessingException {
        final String[] logParts = logLine.split(" ");
        final String source = logParts[0];
        final EventType eventType = EventType.valueOf(logParts[1]);
        final String eventStr = logParts[3];
        final String[] eventProperties = eventStr.split(",");
        if(eventType == EventType.TRANSFER || eventType == ACCOUNT_CONFIG_UPDATE
                || eventType == CONVERT_FUNDS || eventType == MARGIN_CALL) return null;
        AbstractEvent event = objectMapper.readValue(fromPlainToJson(eventProperties), AbstractEvent.class);
        setCommons(date, eventType, source, event);
        switch (eventType) {
            case FUTURES_ACCOUNT_UPDATE:
                AbstractEvent accountUpdateEvent = objectMapper.readValue(fromPlainToJson(eventProperties), AbstractEvent.class);
                setCommons(date, eventType, source, accountUpdateEvent);
                event = accountUpdateEvent;
                break;
            case FUTURES_ORDER_TRADE_UPDATE:
                FuturesOrderTradeUpdateEvent orderTradeUpdateEvent = objectMapper.readValue(fromPlainToJson(eventProperties), FuturesOrderTradeUpdateEvent.class);
                setCommons(date, eventType, source, orderTradeUpdateEvent);
                event = orderTradeUpdateEvent;
                break;
            default:
                System.out.println("LogsEventSource: omitting eventType " + eventType);
        }
        return event;
    }

    private void setCommons(LocalDateTime date, EventType eventType, String source, AbstractEvent event) {
        event.setEventType(eventType);
        event.setDate(date);
        event.setSource(source);
    }
}
