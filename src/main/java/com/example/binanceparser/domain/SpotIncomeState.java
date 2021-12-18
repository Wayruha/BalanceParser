package com.example.binanceparser.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import static com.example.binanceparser.Constants.*;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class SpotIncomeState extends BalanceState {

	transient private Set<Asset> currentAssets;//transient for tests
	transient private Set<AssetState> lockedAssetStates;//transient for tests

	public SpotIncomeState(LocalDateTime dateTime) {
		super(BigDecimal.ZERO, dateTime);
		currentAssets = new HashSet<>();
		lockedAssetStates = new HashSet<>();
	}
	
	public SpotIncomeState(BigDecimal conditionedUSDTBalsnce, LocalDateTime dateTime) {
		super(conditionedUSDTBalsnce, dateTime);
		currentAssets = new HashSet<>();
		lockedAssetStates = new HashSet<>();
	}
	
	public SpotIncomeState(BigDecimal conditionedUSDTBalsnce, LocalDateTime dateTime, SpotIncomeState incomeState) {
		super(conditionedUSDTBalsnce, dateTime);
		currentAssets = new HashSet<>(incomeState.getCurrentAssets());
		lockedAssetStates = new HashSet<>(incomeState.getLockedAssetStates());
	}

	@Data
	public class AssetState extends Asset {
		private String relativeAsset;
		// price of asset relative to relativeAsset. E.g., relativeAsset = USD
		private BigDecimal averagePrice;

		public AssetState(String asset, BigDecimal availableBalance, BigDecimal averagePrice) {
			super(asset, availableBalance);
			this.averagePrice = averagePrice;
			relativeAsset = USD;
		}

		public void updateAssetState(BigDecimal assetDelta, BigDecimal price) {
			if (assetDelta.compareTo(BigDecimal.ZERO) < 0) {
				setBalanceState(getBalanceState()
						.add(assetDelta.negate().multiply(price))// what we got when sold asset
						.subtract(assetDelta.negate().multiply(averagePrice)));// what we spent when bought asset
			}
			
			else {
				averagePrice = averagePrice
						.multiply(getAvailableBalance())
						.add(assetDelta.multiply(price))
						.divide(getAvailableBalance().add(assetDelta));
				setAvailableBalance(getAvailableBalance()
						.add(assetDelta));
			}
		}	
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof AssetState)) {
				return false;
			}
			AssetState as = (AssetState)o;
			return this.getAsset().equals(as.getAsset());
		}
		
		@Override
		public int hashCode() {
			return this.getAsset().hashCode();
		}
	}
	
	public BigDecimal totalBalanceToRelativeAsset() {
		return null;
	}

	public AssetState findAssetState(String assetName) {
		return lockedAssetStates.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}
	
	public void setAssetState(AssetState assetState) {
		lockedAssetStates.add(assetState);
	} 
}
