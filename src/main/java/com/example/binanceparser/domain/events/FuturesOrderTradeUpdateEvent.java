package com.example.binanceparser.domain.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class FuturesOrderTradeUpdateEvent extends OrderEvent {
    @JsonDeserialize(using = NumericBooleanDeserializer.class)
    protected boolean isReduceOnly;
    protected BigDecimal realizedTradeProfit;
    private BigDecimal averagePrice;
    private String stopPrice;
    private String bidsNotional;
    private String askNotional;
    private Boolean isMaker;
    //private WorkingType stopPriceWorkingType;
    //private OrderType originalOrderType;
    //private PositionSide positionSide;
    private Boolean isCloseAll;
    private String activationPrice;
    private String callbackRate;

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
