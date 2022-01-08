package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.binanceparser.Constants.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SpotIncomeState extends BalanceState {
	private Set<Asset> currentAssets;
	//TODO якщо в lockedAssetState лежить ціна 0 - що це означає? мабуть ми не хочемо щоб такі асети лежали у нас
	private Set<LockedAsset> lockedLockedAssets;
	private List<Transaction> transactions;

	public SpotIncomeState(LocalDateTime dateTime) {
		super(BigDecimal.ZERO, dateTime);
		currentAssets = new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, BigDecimal.ZERO)));
		lockedLockedAssets = new LinkedHashSet<>();
		transactions = new ArrayList<>();
	}


	public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
		super(incomeState.getBalanceState(), dateTime);
		currentAssets = new LinkedHashSet<>(incomeState.getCurrentAssets());
		lockedLockedAssets = new LinkedHashSet<>(incomeState.getLockedLockedAssets());
		transactions = new ArrayList<>();
	}

	public BigDecimal calculateVirtualUSDBalance() {
		BigDecimal virtualBalance = BigDecimal.ZERO;
		// works for quoteAsset = USD
		for (LockedAsset lockedAsset : lockedLockedAssets) {
			virtualBalance = virtualBalance
					.add(lockedAsset.getBalance().multiply(lockedAsset.getAveragePrice()));
		}
		return virtualBalance;
	}

	public BigDecimal calculateVirtualUSDBalance(String asset) {
		if (asset.equals(VIRTUAL_USD)) {
			return calculateVirtualUSDBalance();
		}
		// works for quoteAsset = USD
		LockedAsset lockedAsset = findLockedAsset(asset);
		return lockedAsset == null ? BigDecimal.ZERO
				: lockedAsset.getBalance().multiply(lockedAsset.getAveragePrice());
	}

	public BigDecimal totalBalanceToRelativeAsset() {
		BigDecimal sum = BigDecimal.ZERO;

		for (LockedAsset lockedAsset : lockedLockedAssets) {
			sum = sum.add(lockedAsset.getBalance().multiply(lockedAsset.getAveragePrice()));
		}
		return sum;
	}

	public LockedAsset findLockedAsset(String assetName) {
		return lockedLockedAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}

	public LockedAsset addLockedAssetIfNotExist(String assetName) {
		final LockedAsset lockedAsset = findLockedAsset(assetName);
		if (lockedAsset != null)
			return lockedAsset;

		final LockedAsset newAsset = new LockedAsset(assetName, BigDecimal.ZERO, BigDecimal.ZERO);
		lockedLockedAssets.add(newAsset);
		return newAsset;
	}

	public Asset findAsset(String assetName) {
		return currentAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}

	public Asset addAssetIfNotExist(String assetName) {
		final Asset asset = findAsset(assetName);
		if (asset != null)
			return asset;

		final Asset newAsset = new Asset(assetName, BigDecimal.ZERO);
		currentAssets.add(newAsset);
		return newAsset;
	}

	public void removeAssetStateIfEmpty(String assetName) {
		LockedAsset lockedAsset = findLockedAsset(assetName);
		if (lockedAsset != null && lockedAsset.getBalance().compareTo(BigDecimal.ZERO) == 0) {
			lockedLockedAssets.remove(lockedAsset);
		}
	}

	public void updateAssetBalance(List<Asset> updatedAssets) {
		updatedAssets.forEach(updatedAsset -> {
			currentAssets.removeIf(asset -> asset.getAsset().equals(updatedAsset.getAsset()));
			currentAssets.add(updatedAsset);
		});

		// copying current stableCoins to AssetStates set
		STABLECOIN_RATE.keySet().stream().forEach((stableCoin)->{
			addLockedAssetIfNotExist(stableCoin);
			addAssetIfNotExist(stableCoin);
			findLockedAsset(stableCoin).setBalance(findAsset(stableCoin).getBalance());
			findLockedAsset(stableCoin).setAveragePrice(STABLECOIN_RATE.get(stableCoin));
		});
	}

	public TransactionType getLastTransactionType() {
		return transactions.size() != 0 ? transactions.get(transactions.size() - 1).getTransactionType() : null;
	}

	// TODO refactor
	public void processOrderDetails(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
		final LockedAsset lockedAsset = addLockedAssetIfNotExist(assetName);

		if (assetDelta.compareTo(BigDecimal.ZERO) <= 0) {
			if (transactionPrice != null) {
				Transaction transaction;
				if (!TransactionType.WITHDRAW_IN_PROCESS.equals(getLastTransactionType())) {
					transaction = new Transaction(TransactionType.SELL, assetName, lockedAsset.getQuoteAsset(),
							assetDelta, transactionPrice, null);
					transactions.add(transaction);
				} else {
					transaction = transactions.get(transactions.size() - 1);
				}

				BigDecimal maxAssetTrackedAmount = lockedAsset.getBalance().compareTo(assetDelta.abs()) >= 0
						? assetDelta
						: lockedAsset.getBalance().negate();
				BigDecimal transactionIncome = maxAssetTrackedAmount.abs().multiply(transactionPrice)// what we got when
																										// sold asset
						.subtract(maxAssetTrackedAmount.abs().multiply(lockedAsset.getAveragePrice()));// what we spent
																										// when bought
																										// asset
				transaction.setIncome(transactionIncome);
				setBalanceState(getBalanceState().add(transactionIncome));
				lockedAsset.setBalance(lockedAsset.getBalance().subtract(maxAssetTrackedAmount.abs()));// unlock
																														// asset
			} else {
				transactions.add(new Transaction(TransactionType.WITHDRAW_IN_PROCESS, assetName, "", assetDelta,
						transactionPrice, null));
				processOrderDetails(assetName, assetDelta, lockedAsset.getAveragePrice());
				transactions.set(transactions.size() - 1, new Transaction(TransactionType.WITHDRAW, assetName, "",
						assetDelta, transactionPrice, transactions.get(transactions.size() - 1).getIncome()));
			}

			removeAssetStateIfEmpty(assetName);
		} else {
			if (transactionPrice != null) {
				transactions.add(new Transaction(TransactionType.BUY, assetName, lockedAsset.getQuoteAsset(), assetDelta,
						transactionPrice, BigDecimal.ZERO));
				final BigDecimal transactionQuoteQty = assetDelta.multiply(transactionPrice);
				final BigDecimal newAssetQty = lockedAsset.getBalance().add(assetDelta);
				final BigDecimal newCalculatedPrice = lockedAsset.totalQuoteAssetValue().add(transactionQuoteQty)
						.divide(newAssetQty, Constants.MATH_CONTEXT);
				lockedAsset.setAveragePrice(newCalculatedPrice);
				lockedAsset.setBalance(newAssetQty);
				return;
			}
			// else DEPOSIT operation (we do not add deposit to locked assets)
			transactions.add(new Transaction(TransactionType.DEPOSIT, assetName, "", assetDelta, transactionPrice,
					BigDecimal.ZERO));
		}

		findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());
	}

	@Data
	public static class LockedAsset extends Asset {
		private String quoteAsset;
		// price of asset relative to relativeAsset. E.g., relativeAsset = USD
		private BigDecimal averagePrice;

		public LockedAsset(String asset, BigDecimal availableBalance, BigDecimal averagePrice) {
			super(asset, availableBalance);
			this.averagePrice = averagePrice;
			this.quoteAsset = USD;
		}

		public BigDecimal totalQuoteAssetValue() {
			return averagePrice.multiply(balance);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof LockedAsset)) {
				return false;
			}
			LockedAsset as = (LockedAsset) o;
			return this.getAsset().equals(as.getAsset());
		}

		@Override
		public int hashCode() {
			return this.getAsset().hashCode();
		}
	}
}