package com.example.binanceparser.datasource;

import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.TypedEventJsonView;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.binanceparser.datasource.ParserUtil.fromPlainToJson;
import static com.example.binanceparser.domain.events.EventType.*;

/**
 * read directory with logs to provide the events
 */
public class LogsEventSource implements EventSource<AbstractEvent> {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = new ObjectMapper().addMixIn(AbstractEvent.class, TypedEventJsonView.class);;
    private static final List<EventType> IGNORED_EVENTS = List.of(TRANSACTION, TRANSFER, ACCOUNT_CONFIG_UPDATE, CONVERT_FUNDS, MARGIN_CALL, COIN_SWAP_ORDER);
    private final File logsDir;
    private Set<Filter> filters;

    public LogsEventSource(File logsDir, Set<Filter> filters) {
        this.logsDir = logsDir;
        this.filters = filters;
    }

    @Override
    public List<AbstractEvent> getData() {
        final List<AbstractEvent> allEvents = new ArrayList<>();
        try {
            final String[] dirFiles = logsDir.list();
            if (dirFiles == null) throw new RuntimeException("Can`t find any files in directory.");
            Arrays.sort(dirFiles);

            for (String filePath : dirFiles) {
                final File file = new File(logsDir.getAbsolutePath() + "/" + filePath);
                final Document doc = Jsoup.parse(file, "UTF-8");
                final List<Element> messageList = doc.getElementsByClass("info");
                for (Element element : messageList) {
                    final LocalDateTime date = LocalDateTime.parse(element.getElementsByClass("Date").text(), dateFormat);
                    final String logLine = element.getElementsByClass("Message").text();
                    final AbstractEvent event = parseLogLine(date, logLine);
                    if (event != null && fitsToAllFilters(event, filters))
                        allEvents.add(event);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return allEvents;
    }

    public List<String> getUserIds() {
        return getData().stream()
            .map(AbstractEvent::getSource)
            .distinct()
            .collect(Collectors.toList());
    }

    private boolean fitsToAllFilters(AbstractEvent event, Set<Filter> filters) {
        return filters.stream().allMatch(f -> f.filter(event));
    }

    public static AbstractEvent parseLogLine(LocalDateTime date, String logLine) throws IOException {
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

    private static void setCommons(LocalDateTime date, String source, AbstractEvent event, EventType eventType) {
        event.setDateTime(date);
        event.setSource(source);
        event.setEventType(eventType);
    }
}