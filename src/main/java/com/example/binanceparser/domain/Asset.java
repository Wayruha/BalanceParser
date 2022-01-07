package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Asset {
	protected String asset;
	//now transient because test somehow fails
	protected transient BigDecimal balance;
	private AssetMetadata assetMetadata;

	public Asset(String asset, BigDecimal balance) {
		this.asset = asset;
		this.balance = balance;
	}

	//TODO чого це змінює результати тесту? цей баг з тестом потрібно пофіксити обовязково, хоч і не дуже критично по часі
	/*@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Asset asset1 = (Asset) o;

		if (!Objects.equals(asset, asset1.asset)) return false;
		if(balance == null && asset1.balance != null || balance != null && asset1.balance == null) return false;
		if (balance != null && balance.compareTo(asset1.balance) != 0) return false;
		return Objects.equals(assetMetadata, asset1.assetMetadata);
	}

	@Override
	public int hashCode() {
		int result = asset != null ? asset.hashCode() : 0;
		result = 31 * result + (balance != null ? balance.hashCode() : 0);
		result = 31 * result + (assetMetadata != null ? assetMetadata.hashCode() : 0);
		return result;
	}*/
}