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
    private BigDecimal balanceUpdateDelta;
    private Set<Asset> assets;
    //TODO
    //private List<Transaction> transactions;
    // class Transaction: can be Transfer (deposit/withdraw) or Trade

    public EventBalanceState(LocalDate dateTime, Set<Asset> assets, BigDecimal balanceUpdateDelta) {
        super(dateTime);
        this.assets = assets;
        this.balanceUpdateDelta = balanceUpdateDelta;
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
