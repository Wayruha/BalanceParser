package com.example.binanceparser.domain;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "asset")
public class Asset {
    protected String asset;
    //now transient because test somehow fails
    protected BigDecimal balance;
    private AssetMetadata assetMetadata;

    public Asset(String asset, BigDecimal balance) {
        this.asset = asset;
        this.balance = balance;
    }

    public Asset clone() {
        final Asset asset = new Asset(this.asset, balance);
        asset.setAssetMetadata(assetMetadata);
        return asset;
    }
}