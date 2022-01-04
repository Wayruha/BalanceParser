package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class EventBalanceState extends BalanceState {
	private Set<Asset> assets;
	// TODO
	// private List<Transaction> transactions;
	// class Transaction: can be Transfer (deposit/withdraw) or Trade

	public EventBalanceState(LocalDateTime dateTime, Set<Asset> assets, BigDecimal balanceUpdateDelta) {
		super(balanceUpdateDelta, dateTime);
		this.assets = assets;
	}

	public Asset findAsset(String assetName) {
		return assets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}

	public BigDecimal getAssetBalance(String assetName) {
		final Asset asset = findAsset(assetName);
		return asset == null ? BigDecimal.ZERO : asset.getBalance();
	}
}
