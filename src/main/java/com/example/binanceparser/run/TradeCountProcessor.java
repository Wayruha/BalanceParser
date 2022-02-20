package com.example.binanceparser.run;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TradeCountProcessor {
    private static final Logger log = Logger.getLogger(TradeCountProcessor.class.getName());
    private static final String CLONED_ORDER_ENDING = "_cln";
    protected final BalanceVisualizerConfig config;
    final CSVEventSource eventSource;
    private final String subscriber;
    private final String trader;

    public TradeCountProcessor(BalanceVisualizerConfig config, CSVEventSource eventSource) {
        this.config = config;
        this.eventSource = eventSource;
        this.subscriber = "rwdiachuk";
        this.trader = "a.nefedov";
    }

    public static void main(String[] args) throws IOException {
        final AppProperties props = ConfigUtil.loadAppProperties("src/main/resources/application.properties");
        final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(props);
        final CSVEventSource eventSource = getEventSource(config);
        TradeCountProcessor inst = new TradeCountProcessor(config, eventSource);
        inst.process();
    }

    public void process() {
        final List<String> traderIDs = getAllOrders(trader);
        final List<String> subIDs = getAllOrders(subscriber);

        log.info("Distinct traderIDs:" + traderIDs.size() + " " + String.join(",", traderIDs));
        log.info("Distinct subscriberIDs:" + subIDs.size() + " " + String.join(",", subIDs));

        final List<String> notCloned = traderIDs.stream().filter(id -> !subIDs.contains(id)).collect(Collectors.toList());
        final List<String> extra = subIDs.stream().filter(id -> !traderIDs.contains(id)).collect(Collectors.toList());
        log.info("Not cloned by subscriber:" + notCloned.size() + " " + String.join(",", notCloned));
        log.info("Extra created by subscriber:" + extra.size() + " " + String.join(",", extra));

    }

    private List<String> getAllOrders(String user) {
        eventSource.setTrackedPerson(user);
        final List<AbstractEvent> events = eventSource.getData();
        final List<FuturesOrderTradeUpdateEvent> tradeEvents = events.stream().filter(e -> e instanceof FuturesOrderTradeUpdateEvent)
                .map(e -> (FuturesOrderTradeUpdateEvent) e)
                .filter(e -> e.getOrderStatus() == OrderStatus.PARTIALLY_FILLED || e.getOrderStatus() == OrderStatus.FILLED)
                .filter(e->e.getDateTime().isAfter(LocalDate.of(2022, 1, 10).atStartOfDay()))
                .collect(Collectors.toList());
        final List<String> orderIDs = tradeEvents.stream()
                .filter(e -> e.getSource().equals(user))
                .map(e -> e.getNewClientOrderId().replace(CLONED_ORDER_ENDING, ""))
                .distinct()
                .collect(Collectors.toList());
        return orderIDs;
    }

    protected static CSVEventSource getEventSource(BalanceVisualizerConfig config) {
        final File logsDir = new File(config.getInputFilepath());
        CSVEventSource eventSource = new CSVEventSource(logsDir, "");
        return eventSource;
    }
}
