package com.example.binanceparser.domain.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateEvent extends AbstractEvent{

    private String balances;

    private String asset;

    private BigDecimal balanceDelta;

    public void setAsset(String asset){
        this.asset = asset;
        this.balances = asset;
    }

}
