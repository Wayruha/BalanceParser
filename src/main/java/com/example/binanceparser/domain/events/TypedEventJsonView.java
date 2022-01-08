package com.example.binanceparser.domain.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FuturesAccountUpdateEvent.class, name = "FUTURES_ACCOUNT_UPDATE"),
        @JsonSubTypes.Type(value = FuturesOrderTradeUpdateEvent.class, name = "FUTURES_ORDER_TRADE_UPDATE"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "ACCOUNT_UPDATE"),
        @JsonSubTypes.Type(value = AccountPositionUpdateEvent.class, name = "ACCOUNT_POSITION_UPDATE"),
        @JsonSubTypes.Type(value = BalanceUpdateEvent.class, name = "BALANCE_UPDATE"),
        @JsonSubTypes.Type(value = OrderTradeUpdateEvent.class, name = "ORDER_TRADE_UPDATE"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "LISTEN_KEY_EXPIRED"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "ACCOUNT_CONFIG_UPDATE"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "COIN_SWAP_ORDER"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "MARGIN_CALL"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "TRANSFER"),
        @JsonSubTypes.Type(value = OtherEvents.class, name = "MARGIN_CALL"),
})
public interface TypedEventJsonView {
}
