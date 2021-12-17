package com.example.binanceparser.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class SpotIncomeState extends BalanceState {

	private static BigDecimal conditionedUSDTBalsnce;
	transient private static Set<AssetState> lockedAssetStates;//transient for tests

	static {
		conditionedUSDTBalsnce = BigDecimal.ZERO;
		lockedAssetStates = new HashSet<>();
	}

	public SpotIncomeState(LocalDateTime dateTime) {
		super(conditionedUSDTBalsnce, dateTime);
	}
	
	public SpotIncomeState(BigDecimal conditionedUSDTBalsnce, LocalDateTime dateTime) {
		super(conditionedUSDTBalsnce, dateTime);
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class AssetState extends Asset {

		private BigDecimal averagePrice;

		public AssetState(String asset, BigDecimal availableBalance, BigDecimal averagePrice) {
			super(asset, availableBalance);
			this.averagePrice = averagePrice;
		}

		public void updateAssetState(BigDecimal assetDelta, BigDecimal price) {

			if (assetDelta.compareTo(BigDecimal.ZERO) < 0) {
				setBalanceState(getBalanceState()
						.add(assetDelta.multiply(price))// what we got when sold asset
						.subtract(assetDelta.multiply(averagePrice)));// what we spent when bought asset
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
		
	}

	public AssetState findAssetState(String assetName) {
		return lockedAssetStates.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
	}
	
	//TODO should handle multiple assetStates for one asset problem 
	public void setAssetState(AssetState assetState) {
		lockedAssetStates.add(assetState);
	} 
	
}
