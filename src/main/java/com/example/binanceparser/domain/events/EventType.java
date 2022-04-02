package com.example.binanceparser.domain.events;

import com.fasterxml.jackson.annotation.JsonValue;


public enum EventType {
    ACCOUNT_UPDATE("ACCOUNT_UPDATE"),
    ACCOUNT_POSITION_UPDATE("ACCOUNT_POSITION_UPDATE"),
    BALANCE_UPDATE("BALANCE_UPDATE"),
    ORDER_TRADE_UPDATE("ORDER_TRADE_UPDATE"),
    FUTURES_ORDER_TRADE_UPDATE("FUTURES_ORDER_TRADE_UPDATE"),
    FUTURES_ACCOUNT_UPDATE("FUTURES_ACCOUNT_UPDATE"),
    LISTEN_KEY_EXPIRED("LISTEN_KEY_EXPIRED"),
    ACCOUNT_CONFIG_UPDATE("ACCOUNT_CONFIG_UPDATE"),
    COIN_SWAP_ORDER("COIN_SWAP_ORDER"),
    MARGIN_CALL("MARGIN_CALL"),
    OCO_TRADE_UPDATE("OCO_TRADE_UPDATE"),

    //custom types, Binance does not know about them
    TRANSFER("TRANSFER"),
    TRANSACTION("TRANSACTION"),
    CONVERT_FUNDS("CONVERT_FUNDS"),

    //TODO temporary workaround to handle different event names coming from different event sources: logs and db (csv)
    // only valid if we DO NOT reference these enum values in code
    ACCOUNT_POSITION_UPDATE2("outboundAccountPosition"),
    ORDER_TRADE_UPDATE2("executionReport"),
    BALANCE_UPDATE2("balanceUpdate"),
    ;

    private final String eventTypeId;

    EventType(String eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    @JsonValue
    public String getEventTypeId() {
        return eventTypeId;
    }

}
