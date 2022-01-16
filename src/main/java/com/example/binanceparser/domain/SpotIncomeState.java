package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SpotIncomeState extends BalanceState {
    private Map<String, Asset> currentAssets;
    private Map<String, LockedAsset> lockedAssets;
    private List<Transaction> transactions;
    private List<TransactionX> TXs;

    public SpotIncomeState(LocalDateTime dateTime) {
        super(ZERO, dateTime);
        currentAssets = List.of(new Asset(VIRTUAL_USD, ZERO)).stream().collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        lockedAssets = new HashMap<>();
        transactions = new ArrayList<>();
        TXs = new ArrayList<>();
    }

    public SpotIncomeState(Set<Asset> currentAssets, Set<LockedAsset> lockedAssets, List<Transaction> transactions, List<TransactionX> TXs) {
        this.currentAssets = currentAssets.stream().collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.lockedAssets = lockedAssets.stream().collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.transactions = transactions;
        this.TXs = TXs;
    }

    public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
        super(null, dateTime);
        this.currentAssets = incomeState.getCurrentAssets().stream().map(Asset::clone).collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.lockedAssets = incomeState.getLockedAssets().stream().map(LockedAsset::clone).collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.transactions = new ArrayList<>();
        TXs = new ArrayList<>();
    }

    public void setBalanceState(BigDecimal v) {
        throw new RuntimeException("Do not use balanceState at all!");
    }

    public BigDecimal getBalanceState() {
        throw new RuntimeException("Do not use balanceState at all!");
    }

    public BigDecimal calculateVirtualUSDBalance() {
        BigDecimal virtualBalance = ZERO;
        // works for quoteAsset = USD
        for (LockedAsset lockedAsset : lockedAssets.values()) {
            virtualBalance = virtualBalance.add(lockedAsset.getStableValue());
        }
        return virtualBalance.round(Constants.MATH_CONTEXT);
    }

    public BigDecimal calculateVirtualUSDBalance(String asset) {
        if (asset.equals(VIRTUAL_USD)) {
            return calculateVirtualUSDBalance();
        }
        // works for quoteAsset = USD
        final Optional<LockedAsset> lockedAsset = findLockedAsset(asset);
        return lockedAsset.map(LockedAsset::getStableValue).orElse(ZERO);
    }

  /*  public LockedAsset findLockedAsset(String assetName) {
        return lockedLockedAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
    }*/

    public Optional<LockedAsset> findLockedAsset(String assetName) {
        return ofNullable(lockedAssets.get(assetName));
    }

    public LockedAsset addLockedAssetIfNotExist(String assetName) {
        lockedAssets.putIfAbsent(assetName, LockedAsset.empty(assetName));
        return lockedAssets.get(assetName);
    }

    public List<Asset> getExistingStablecoins() {
        return currentAssets.values().stream()
                .filter(a -> STABLECOIN_RATE.containsKey(a.getAsset()))
                .collect(Collectors.toList());
    }

    public Optional<Asset> findAssetOpt(String assetName) {
        return ofNullable(currentAssets.get(assetName));
    }

   /* public Asset addAssetIfNotExist(String assetName) {
        final Asset asset = findAsset(assetName);
        if (asset != null)
            return asset;

        final Asset newAsset = new Asset(assetName, ZERO);
        currentAssets.put(assetName, newAsset);
        return newAsset;
    }*/

    public void removeLockedStateIfEmpty(String... assetNames) {
        for (String assetName : assetNames) {
            final Optional<LockedAsset> optionalLockedAsset = findLockedAsset(assetName);
            if (optionalLockedAsset.isPresent() && optionalLockedAsset.get().getBalance().signum() == 0) {
                lockedAssets.remove(assetName);
            }
        }
    }

    public void addAsset(Asset asset) {
        currentAssets.put(asset.getAsset(), asset);
    }

    public void addLockedAsset(LockedAsset asset) {
        lockedAssets.put(asset.getAsset(), asset);
    }

    public Set<Asset> getCurrentAssets() {
        return Set.copyOf(currentAssets.values());
    }

    public Set<LockedAsset> getLockedAssets() {
        return Set.copyOf(lockedAssets.values());
    }

    public boolean removeAsset(Asset asset) {
        return currentAssets.remove(asset.getAsset()) != null;
    }

    public void processOrderDetails(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
    /*    final LockedAsset lockedAsset = addLockedAssetIfNotExist(assetName);
        if (assetDelta.compareTo(BigDecimal.ZERO) <= 0) {
            if (transactionPrice != null) {
                handleSell(assetName, assetDelta, transactionPrice, lockedAsset);
            } else {
                handleWithdraw(assetName, assetDelta, transactionPrice, lockedAsset);
            }
            removeAssetStateIfEmpty(assetName);
        } else {
            if (transactionPrice != null) {
                handleBuy(assetName, assetDelta, transactionPrice, lockedAsset);
                findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());
                return;
            }
            handleDeposit(assetName, assetDelta, transactionPrice);
        }
        findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());*/
    }

    public void updateAssetsBalance(List<Asset> updatedAssets) {
        updatedAssets.forEach(updatedAsset -> {
            final Optional<Asset> assetOpt = findAssetOpt(updatedAsset.getAsset());
            assetOpt.ifPresentOrElse(asset -> asset.setBalance(updatedAsset.getBalance()),
                    () -> addAsset(updatedAsset));
            removeLockedStateIfEmpty(updatedAsset.getAsset());
        });
        // copying current stableCoins to AssetStates set
        getExistingStablecoins().forEach(stableCoin -> {
            final LockedAsset lockedAsset = addLockedAssetIfNotExist(stableCoin.getAsset());
            lockedAsset.setBalance(stableCoin.getBalance());
            lockedAsset.setStableValue(stableCoin.getBalance().multiply(STABLECOIN_RATE.get(stableCoin.getAsset())));
        });
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class LockedAsset extends Asset {
        private String quoteAsset;
        // price of this asset relative to quoteAsset. E.g., relativeAsset = USD
        private BigDecimal averageQuotePrice;
        private BigDecimal stableValue;

        public LockedAsset(String asset, BigDecimal availableBalance, String quoteAsset, BigDecimal stableValue) {
            super(asset, availableBalance);
            this.quoteAsset = quoteAsset;
            this.stableValue = stableValue;
        }

        public LockedAsset(String asset, BigDecimal availableBalance, BigDecimal stableValue) {
            super(asset, availableBalance);
            this.quoteAsset = null;
            this.averageQuotePrice = null;
            this.stableValue = stableValue;
        }

        public static LockedAsset empty(String assetName) {
            return new LockedAsset(assetName, ZERO, null, ZERO);
        }

        public LockedAsset clone() {
            LockedAsset asset = new LockedAsset(super.asset, super.balance, quoteAsset, stableValue);
            asset.setQuoteAsset(quoteAsset);
            return asset;
        }

       /* public BigDecimal totalQuoteAssetValue() {
            return averageQuotePrice.multiply(balance);
        }*/

        public void deductBalance(BigDecimal qty) {
            if (qty.signum() == 0) return;
            if (balance.compareTo(qty) < 0)
                System.out.println("Deducting more than exists. qty=" + qty.toPlainString() + ". " + toString());
            final BigDecimal qtyToBalanceFraction = qty.divide(balance, MATH_CONTEXT);
            balance = balance.subtract(qty);
            stableValue = stableValue.subtract(stableValue.multiply(qtyToBalanceFraction, MATH_CONTEXT));
        }

        public void addBalance(BigDecimal qty, BigDecimal stableValue) {
            if (stableValue.signum() == 0) {
                throw new IllegalArgumentException("stableValue can't be null in LockedAsset");
            }
            this.balance = this.balance.add(qty);
            this.stableValue = this.stableValue.add(stableValue);
        }
    }
}