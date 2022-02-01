package com.example.binanceparser.report.enricher;

import com.binance.api.client.domain.ExecutionType;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.example.binanceparser.report.BalanceReport;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TradeCountReportEnricher implements ReportEnricher<AbstractEvent> {

    @Override
    public BalanceReport enrichReport(BalanceReport report, List<AbstractEvent> events) {
        final Set<String> tradeIds = events.stream()
                .filter(event -> event instanceof FuturesOrderTradeUpdateEvent)
                .map(event -> (FuturesOrderTradeUpdateEvent) event)
                .filter(trade -> trade.getExecutionType() == ExecutionType.TRADE)
                .map(FuturesOrderTradeUpdateEvent::getNewClientOrderId)
                .collect(Collectors.toSet());
        report.setTotalTradeTxCount_2(tradeIds.size());
        return report;
    }
}
