package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.security.cert.CertPathValidatorException.Reason;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class EventBalanceState extends BalanceState {
	private Set<Asset> assets;
	private List<Transaction> transactions;

	public EventBalanceState(LocalDateTime dateTime, Set<Asset> assets, BigDecimal balanceUpdateDelta) {
		super(balanceUpdateDelta, dateTime);
		this.assets = assets;
	}
	
	//TODO
	public void processReasonType(AccountUpdateReasonType reasonType) {

	}

	public Asset findAsset(String assetName) {
		return assets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}

	public BigDecimal getAssetBalance(String assetName) {
		final Asset asset = findAsset(assetName);
		return asset == null ? BigDecimal.ZERO : asset.getBalance();
	}
}
