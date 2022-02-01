package com.example.binanceparser.domain.events;

import com.binance.api.client.domain.OrderSide;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.IOException;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class FuturesOrderTradeUpdateEvent extends AbstractEvent {

    private String symbol;

    @JsonDeserialize(using = NumericBooleanDeserializer.class)
    private boolean isReduceOnly;

    private String orderStatus;

    private Double originalQuantity;

    private Double price;

    private Double accumulatedQuantity;

    private OrderSide side;

    public boolean isReduceOnly() {
        return isReduceOnly;
    }

    public static class NumericBooleanDeserializer extends JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return !"0".equals(parser.getText());
        }
    }

}
