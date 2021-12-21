package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
        super(incomeState.getBalanceState(), dateTime);
        currentAssets = new HashSet<>(incomeState.getCurrentAssets());
        lockedAssetStates = new HashSet<>(incomeState.getLockedAssetStates());
    }

    public BigDecimal totalBalanceToRelativeAsset() {
        BigDecimal sum = new BigDecimal(0);
        for (Asset asset : currentAssets) {
            if (asset.getAsset().equals(USDT) || asset.getAsset().equals(BUSD)) {
                sum = sum.add(asset.getAvailableBalance());
            }
        }

        for (AssetState assetState : lockedAssetStates) {
            sum = sum.add(assetState.getAvailableBalance().multiply(assetState.getAveragePrice()));
        }
        return sum;
    }

    public AssetState findAssetState(String assetName) {
        return lockedAssetStates.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
    }

    public AssetState addAssetIfNotExist(String assetName) {
        final AssetState assetState = findAssetState(assetName);
        if (assetState != null) return assetState;

        final AssetState newAsset = new AssetState(assetName, BigDecimal.ZERO, BigDecimal.ZERO);
        lockedAssetStates.add(newAsset);
        return newAsset;
    }

    public void updateAssetBalance(List<Asset> updatedAssets) {
        updatedAssets.forEach((updatedAsset) -> {
            currentAssets.removeIf((currentAsset) -> currentAsset.getAsset().equals(updatedAsset.getAsset()));
            currentAssets.add(updatedAsset);
        });
    }

    //TODO  AssetState i BalanceState - була намішана логіка і розмиті сфери відповідальності
    // публічні методи внутрішнього класу використоувалися для зміни стану зовнішнього обєкта.
    // - один метод AssetState змінював і себе і зовншій клас одночасно
    public void processOrderDetails(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
        final AssetState asset = addAssetIfNotExist(assetName);

        if (assetDelta.compareTo(BigDecimal.ZERO) < 0) {
            //SELL operation
            //TODO а Ассет то поки що не пропадає із lockedBalanceState, хоча ми його вже продали і роз-лочили кошти
            setBalanceState(getBalanceState()
                    .add(assetDelta.negate().multiply(transactionPrice))// what we got when sold asset
                    .subtract(assetDelta.negate().multiply(asset.getAveragePrice())));// what we spent when bought asset
        } else {
            //BUY operation
            final BigDecimal transactionQuoteQty = assetDelta.multiply(transactionPrice);
            final BigDecimal newAssetQty = asset.getAvailableBalance().add(assetDelta);
            final BigDecimal newCalculatedPrice = asset.totalQuoteAssetValue()
                    .add(transactionQuoteQty)
                    .divide(newAssetQty, Constants.MATH_CONTEXT);
            asset.setAveragePrice(newCalculatedPrice);
            asset.setAvailableBalance(newAssetQty);
        }
    }

    @Data
    public static class AssetState extends Asset {
        //remove_me quoteAsset - той в якому ми відображаємо ціну. baseAsset = BTC, quoteAsset = USD (чи USDT)  - той, яким ми платимо
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