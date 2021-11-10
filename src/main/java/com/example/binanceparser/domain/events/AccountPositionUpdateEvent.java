package com.example.binanceparser.domain.events;

import com.example.binanceparser.domain.events.AbstractEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountPositionUpdateEvent extends AbstractEvent {

    public List<Asset> balances;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Asset{
        String asset;
        BigDecimal free;
        BigDecimal locked;
    }
}
