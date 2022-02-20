package com.example.binanceparser.run;

import com.binance.api.client.domain.ExecutionType;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.SetUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TradeListComparatorRunner {
    private static final Logger log = Logger.getLogger(TradeListComparatorRunner.class.getName());

    private final AppProperties appProperties;
    private final BalanceVisualizerConfig config;
    private final EventSource<AbstractEvent> eventSource;
    private String trader;
    private List<String> followers;

    public static void main(String[] args) throws IOException {
        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/application.properties");
        final TradeListComparatorRunner inst = new TradeListComparatorRunner(appProperties);
        inst.compareTradeLists();
    }

    public TradeListComparatorRunner(AppProperties appProperties) {
        final List<String> users = appProperties.getTrackedPersons();
        this.appProperties = appProperties;
        this.config = ConfigUtil.loadVisualizerConfig(appProperties);
        config.setSubject(users);
        this.eventSource = BalanceStateVisualizer.getEventSource(appProperties.getDataSourceType(), config);
        this.trader = users.get(0);
        this.followers = users.subList(1, users.size());
    }

    public void compareTradeLists() {
        final EventSource<AbstractEvent> eventSource = BalanceStateVisualizer.getEventSource(appProperties.getDataSourceType(), config);
        final List<FuturesOrderTradeUpdateEvent> allEvents = eventSource.getData().stream()
                .filter(event -> filters(config).stream().allMatch(filter -> filter.filter(event)))
                .filter(e -> e instanceof FuturesOrderTradeUpdateEvent)
                .map(e -> (FuturesOrderTradeUpdateEvent) e)
                .collect(Collectors.toList());

        final List<Report> reports = new ArrayList<>();
        final List<FuturesOrderTradeUpdateEvent> traderEvents = getUserEvents(trader, allEvents);
        final Set<String> traderOrders = extractOrderIds(traderEvents);

        for (String follower : followers) {
            final List<FuturesOrderTradeUpdateEvent> userEvents = getUserEvents(follower, allEvents);
            final Set<String> userOrders = extractOrderIds(userEvents);
            Report report = new Report(follower, trader);
            report.setOrderIds(userOrders);
            report.setExtraOrders(SetUtils.difference(userOrders, traderOrders));
            report.setMissingOrders(SetUtils.disjunction(traderOrders, userOrders));
            reports.add(report);
        }
        reports.forEach(r -> log.info(r.json()));
    }

    public List<FuturesOrderTradeUpdateEvent> getUserEvents(String user, List<FuturesOrderTradeUpdateEvent> events) {
        return events.stream()
                .filter(e -> e.getSource().equals(user))
                .collect(Collectors.toList());
    }

    public Set<String> extractOrderIds(List<FuturesOrderTradeUpdateEvent> events) {
        return events.stream()
                .filter(trade -> trade.getExecutionType() == ExecutionType.TRADE)
                .map(FuturesOrderTradeUpdateEvent::getNewClientOrderId)
                .collect(Collectors.toSet());
    }

    private static Set<Filter> filters(BalanceVisualizerConfig config) {
        final Set<Filter> filters = new HashSet<>();
        if (config.getStartTrackDate() != null || config.getFinishTrackDate() != null)
            filters.add(new DateEventFilter(config.getStartTrackDate(), config.getFinishTrackDate()));

        if (config.getSubject() != null) filters.add(new SourceFilter(config.getSubject()));

        if (config.getEventType() != null) filters.add(new EventTypeFilter(config.getEventType()));
        return filters;
    }

    @Getter
    @Setter
    class Report {
        private String follower;
        private String trader;
        private Set<String> orderIds;
        private Set<String> missingOrders;
        private Set<String> extraOrders;

        public Report(String follower, String trader) {
            this.follower = follower;
            this.trader = trader;
        }

        public String json() {
            try {
                return new ObjectMapper().writer().writeValueAsString(this);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
