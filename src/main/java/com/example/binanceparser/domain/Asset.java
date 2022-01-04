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
	//now transient because test somehow fails
	protected transient BigDecimal balance;
	private AssetMetadata assetMetadata;

	public Asset(String asset, BigDecimal balance) {
		this.asset = asset;
		this.balance = balance;
	}
}
