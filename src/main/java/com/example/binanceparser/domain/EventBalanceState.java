package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBalanceState extends BalanceState {
    private boolean balanceUpdate;
    private Set<Asset> assets;

    public EventBalanceState(LocalDate dateTime, Set<Asset> assets, boolean balanceUpdate) {
        super(dateTime);
        this.assets = assets;
        this.balanceUpdate = balanceUpdate;
    }

    public Asset findAsset(String assetName) {
        return assets.stream()
                .filter(a -> a.getAsset().equals(assetName))
                .findFirst()
                .orElse(null);
    }

    public BigDecimal getAssetBalance(String assetName) {
        final Asset asset = findAsset(assetName);
        return asset == null ? BigDecimal.ZERO : asset.getAvailableBalance();
    }
}
