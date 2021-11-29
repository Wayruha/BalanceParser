package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBalanceState extends BalanceState {

    boolean balanceUpdate;

    Set<Asset> assets;

    public EventBalanceState(LocalDate dateTime, Set<Asset> assets, boolean balanceUpdate) {
        super(dateTime);
        this.assets = assets;
        this.balanceUpdate = balanceUpdate;
    }

}
