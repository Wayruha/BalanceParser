package com.example.binanceparser.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum EventType {
    ACCOUNT_UPDATE("ACCOUNT_UPDATE"),
    ACCOUNT_POSITION_UPDATE("ACCOUNT_POSITION_UPDATE"),
    BALANCE_UPDATE("BALANCE_UPDATE"),
    ORDER_TRADE_UPDATE("ORDER_TRADE_UPDATE"),
    FUTURES_ORDER_TRADE_UPDATE("FUTURES_ORDER_TRADE_UPDATE"),// needed
    FUTURES_ACCOUNT_UPDATE("FUTURES_ACCOUNT_UPDATE"), // needed створюється, коли аккаунт щось купляє/продає. показує актуальний стан балансу аккаунту:
    // монети які містить аккаунт і відкриті на даний момент позиції (про позиції пишу нижче).
    // Містить інформацію по торговій парі: ціна, кількість, статус і т.д.
    // Може мати різні orderStatus, нас цікавить лише orderStatus=FILLED (означає, що заявка на покупку/продажу виконана повністю) - інші можна ігнорувати.
    LISTEN_KEY_EXPIRED("LISTEN_KEY_EXPIRED"),
    ACCOUNT_CONFIG_UPDATE("ACCOUNT_CONFIG_UPDATE"), //only for leverage change in Binance Futures,
    COIN_SWAP_ORDER("COIN_SWAP_ORDER"),
    MARGIN_CALL("MARGIN_CALL"),

    //custom types, Binance does not know about them
    TRANSFER("TRANSFER"),
    CONVERT_FUNDS("CONVERT_FUNDS");

    private final String eventTypeId;

    EventType(String eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    @JsonValue
    public String getEventTypeId() {
        return eventTypeId;
    }


/*    public static EventType fromEventTypeId(String eventTypeId) {
        return Stream.of(values()).filter(type -> type.eventTypeId.equals(eventTypeId)).findFirst()
                .orElseThrow(() -> new UnsupportedEventException("Unrecognized user data update event type id: " + eventTypeId));
    }*/
}
