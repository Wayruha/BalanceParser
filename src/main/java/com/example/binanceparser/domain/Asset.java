package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Asset {
	protected String asset;
	protected BigDecimal availableBalance;
	private AssetMetadata assetMetadata;
	
	public Asset(String asset, BigDecimal availableBalance) {
		this.asset = asset;
		this.availableBalance = availableBalance;
	}
}
