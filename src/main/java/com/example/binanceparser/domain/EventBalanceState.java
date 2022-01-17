package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.Constants.*;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class EventBalanceState extends BalanceState {
	private Set<Asset> assets;
	private List<Transaction> transactions;

	public EventBalanceState(LocalDateTime dateTime, BigDecimal balanceUpdateDelta) {
		super(dateTime);
		// TODO add virtual usd
		assets = new LinkedHashSet<>();
		transactions = new ArrayList<>();
	}

	public EventBalanceState(LocalDateTime dateTime, List<Asset> assets, BigDecimal balanceUpdateDelta) {
		super(dateTime);
		// TODO add virtual usd
		this.assets = new LinkedHashSet<>(assets);
		transactions = new ArrayList<>();
	}
	
	public EventBalanceState(LocalDateTime dateTime, EventBalanceState balanceState, BigDecimal balanceUpdateDelta) {
		super(dateTime);
		assets = new LinkedHashSet<>(balanceState.getAssets());
		transactions = new ArrayList<>();
	}
	
	public void updateAssets(List<Asset> newAssets) {
		newAssets.stream().forEach((updatedAsset) -> {
			assets.removeIf((currentAsset) -> currentAsset.getAsset().equals(updatedAsset.getAsset()));
			assets.add(updatedAsset);
		});
	}

	public void processOrderDetails(AccountUpdateReasonType reasonType, String baseAsset, String quoteAsset,
			BigDecimal assetDelta, BigDecimal price) {
		if (reasonType.equals(AccountUpdateReasonType.WITHDRAW)) {
			transactions.add(new Transaction(TransactionType.WITHDRAW, baseAsset, quoteAsset, assetDelta, price, null));
		} else if (reasonType.equals(AccountUpdateReasonType.DEPOSIT)) {
			transactions.add(new Transaction(TransactionType.DEPOSIT, baseAsset, quoteAsset, assetDelta, price, null));
		} else {
			// TODO handle all other types
			transactions.add(new Transaction());
		}
	}
	
	//will be rewrited entirely 
	public BigDecimal calculateVirtualUSDBalance() {
		BigDecimal virtualBalance = BigDecimal.ZERO;
		// works for quoteAsset = USD
		for (Asset asset : assets) {
			if(STABLECOIN_RATE.containsKey(asset.getAsset())) {
				virtualBalance = virtualBalance.add(STABLECOIN_RATE.get(asset.getAsset()).multiply(asset.getBalance()));
			}
		}
		return virtualBalance;
	}

	public Asset findAsset(String assetName) {
		return assets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}

	public BigDecimal getAssetBalance(String assetName) {
		//this will be removed later
		if(assetName.equals(VIRTUAL_USD)) {
			return calculateVirtualUSDBalance();
		}
		
		final Asset asset = findAsset(assetName);
		return asset == null ? BigDecimal.ZERO : asset.getBalance();
	}
}