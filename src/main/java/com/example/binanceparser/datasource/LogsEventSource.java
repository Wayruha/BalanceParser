package com.example.binanceparser.datasource;

import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;
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
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.datasource.ParserUtil.fromPlainToJson;
import static com.example.binanceparser.domain.events.EventType.*;

/**
 * read directory with logs to provide the events
 */
public class LogsEventSource implements EventSource {
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    final ObjectMapper objectMapper = new ObjectMapper();
    public static final List<EventType> IGNORED_EVENTS = List.of(TRANSFER, ACCOUNT_CONFIG_UPDATE, CONVERT_FUNDS, MARGIN_CALL, COIN_SWAP_ORDER);


    public List<AbstractEvent> readEvents(File logsDir, Set<Filter> filters) throws IOException {
        String[] dirFiles = logsDir.list();
        if (dirFiles == null) throw new RuntimeException("Can`t find any files in directory.");

        List<AbstractEvent> allEvents = new ArrayList<>();
        for (String filePath : dirFiles) {
            File file = new File(logsDir.getAbsolutePath() + "/" + filePath);
            Document doc = Jsoup.parse(file, "UTF-8");
            List<Element> messageList = doc.getElementsByClass("info");
//            messageList.remove(0); // remove first element of logs table (bc it`s table header)
            for (Element element : messageList) {
                LocalDateTime date = LocalDateTime.parse(element.getElementsByClass("Date").text(), dateFormat);
                final String logLine = element.getElementsByClass("Message").text();
                final AbstractEvent event = parseLogLine(date, logLine);
                if (event != null && fitsToAllFilters(event, filters))
                    allEvents.add(event);
            }
        }
        return allEvents;
    }

    private boolean fitsToAllFilters(AbstractEvent event, Set<Filter> filters) {
        return filters.stream().allMatch(f -> f.filter(event));
    }

    public AbstractEvent parseLogLine(LocalDateTime date, String logLine) throws JsonProcessingException {
        final String[] logParts = logLine.split(" ");
        final String source = logParts[0];
        final EventType eventType = valueOf(logParts[1]);
        final String eventStr = logParts[3];
        final String[] eventProperties = eventStr.split(",");
        if (IGNORED_EVENTS.contains(eventType)) return null;
        AbstractEvent event = objectMapper.readValue(fromPlainToJson(eventProperties), AbstractEvent.class);
        setCommons(date, source, event, eventType);// parser don`t parse correctly eventType for some events
        return event;
    }

    private void setCommons(LocalDateTime date, String source, AbstractEvent event, EventType eventType) {
        event.setDate(date);
        event.setSource(source);
        event.setEventType(eventType);
    }
}
