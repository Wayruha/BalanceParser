package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.Constants.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SpotIncomeState extends BalanceState {
    transient private Set<Asset> currentAssets;//transient for tests
    transient private Set<AssetState> lockedAssetStates;//transient for tests
    transient private List<Transaction> transactions;// transient for tests
    
    public SpotIncomeState(LocalDateTime dateTime) {
        super(BigDecimal.ZERO, dateTime);
        currentAssets = new HashSet<>();
        lockedAssetStates = new HashSet<>();
        transactions = new ArrayList<>();
    }

    public SpotIncomeState(BigDecimal conditionedUSDTBalsnce, LocalDateTime dateTime) {
        super(conditionedUSDTBalsnce, dateTime);
        currentAssets = new HashSet<>();
        lockedAssetStates = new HashSet<>();
        transactions = new ArrayList<>();
    }

    public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
        super(incomeState.getBalanceState(), dateTime);
        currentAssets = new HashSet<>(incomeState.getCurrentAssets());
        lockedAssetStates = new HashSet<>(incomeState.getLockedAssetStates());
        transactions = new ArrayList<>();
    }

    public BigDecimal totalBalanceToRelativeAsset() {
		BigDecimal sum = new BigDecimal(0);

		for (AssetState assetState : lockedAssetStates) {
			sum = sum.add(assetState.getAvailableBalance().multiply(assetState.getAveragePrice()));
		}
		return sum;
	}

    public AssetState findAssetState(String assetName) {
        return lockedAssetStates.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
    }

    public AssetState addAssetStateIfNotExist(String assetName) {
		final AssetState assetState = findAssetState(assetName);
		if (assetState != null)
			return assetState;

		final AssetState newAsset = new AssetState(assetName, BigDecimal.ZERO, BigDecimal.ZERO);
		lockedAssetStates.add(newAsset);
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
		AssetState assetState = findAssetState(assetName);
		if (assetState!=null && assetState.getAvailableBalance().compareTo(BigDecimal.ZERO) == 0) {
			lockedAssetStates.remove(assetState);
		}
	}

    public void updateAssetBalance(List<Asset> updatedAssets) {
    	updatedAssets.stream().forEach((updatedAsset) -> {
			currentAssets.removeIf((currentAsset) -> currentAsset.getAsset().equals(updatedAsset.getAsset()));
			currentAssets.add(updatedAsset);
		});

		// copying current stableCoins to AssetStates set
    	addAssetStateIfNotExist(USDT).setAvailableBalance(addAssetIfNotExist(USDT).getAvailableBalance());
    	findAssetState(USDT).setAveragePrice(BigDecimal.ONE);
    	addAssetStateIfNotExist(BUSD).setAvailableBalance(addAssetIfNotExist(BUSD).getAvailableBalance());
    	findAssetState(BUSD).setAveragePrice(BigDecimal.ONE);
    }
    
	public Transaction getLastTransaction() {
		return transactions.size() != 0 ? transactions.get(transactions.size() - 1) : null;
	}

    public void processOrderDetails(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
    	final AssetState assetState = addAssetStateIfNotExist(assetName);
    	
		if (assetDelta.compareTo(BigDecimal.ZERO) <= 0) {
			if (transactionPrice != null) {
				if (!Transaction.WITHDRAW_IN_PROCESS.equals(getLastTransaction())) {
					transactions.add(Transaction.SELL);
				}
				
				BigDecimal maxAssetTrackedAmount = assetState.getAvailableBalance().compareTo(assetDelta.abs()) >= 0 ? assetDelta
						: assetState.getAvailableBalance().negate();
				setBalanceState(getBalanceState().add(maxAssetTrackedAmount.abs().multiply(transactionPrice))// what we got when sold asset
						.subtract(maxAssetTrackedAmount.abs().multiply(assetState.getAveragePrice())));// what we spent when bought asset
				assetState.setAvailableBalance(assetState.getAvailableBalance().subtract(maxAssetTrackedAmount.abs()));//unlock asset
			} else {
				transactions.add(Transaction.WITHDRAW_IN_PROCESS);
				processOrderDetails(assetName, assetDelta, assetState.getAveragePrice());
				transactions.set(transactions.size() - 1, Transaction.WITHDRAW);
			}

			removeAssetStateIfEmpty(assetName);
		} else {
			if (transactionPrice != null) {
				transactions.add(Transaction.BUY);
				final BigDecimal transactionQuoteQty = assetDelta.multiply(transactionPrice);
				final BigDecimal newAssetQty = assetState.getAvailableBalance().add(assetDelta);
				final BigDecimal newCalculatedPrice = assetState.totalQuoteAssetValue().add(transactionQuoteQty)
						.divide(newAssetQty, Constants.MATH_CONTEXT);
				assetState.setAveragePrice(newCalculatedPrice);
				assetState.setAvailableBalance(newAssetQty);
				return;
			}
			//else DEPOSIT operation (we do not add deposit to locked assets)
			transactions.add(Transaction.DEPOSIT);
		}
    }

    @Data
    public static class AssetState extends Asset {
        private String quoteAsset;
        // price of asset relative to relativeAsset. E.g., relativeAsset = USD
        private BigDecimal averagePrice;

        public AssetState(String asset, BigDecimal availableBalance, BigDecimal averagePrice) {
            super(asset, availableBalance);
            this.averagePrice = averagePrice;
            this.quoteAsset = USD;
        }

        public BigDecimal totalQuoteAssetValue() {
            return averagePrice.multiply(availableBalance);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof AssetState)) {
                return false;
            }
            AssetState as = (AssetState) o;
            return this.getAsset().equals(as.getAsset());
        }

        @Override
        public int hashCode() {
            return this.getAsset().hashCode();
        }
    }
}