package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.STABLECOIN_RATE;
import static com.example.binanceparser.Constants.VIRTUAL_USD;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SpotIncomeState extends BalanceState {
    private Map<String, Asset> currentAssets;
    private Map<String, LockedAsset> lockedAssets;
    private List<TransactionX> TXs;

    public SpotIncomeState(LocalDateTime dateTime) {
        super(dateTime);
        currentAssets = List.of(new Asset(VIRTUAL_USD, ZERO)).stream().collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        lockedAssets = new HashMap<>();
        TXs = new ArrayList<>();
    }

    public SpotIncomeState(Set<Asset> currentAssets, Set<LockedAsset> lockedAssets, List<Transaction> transactions, List<TransactionX> TXs) {
        this.currentAssets = currentAssets.stream().collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.lockedAssets = lockedAssets.stream().collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.TXs = TXs;
    }

    public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
        super(dateTime);
        this.currentAssets = incomeState.getCurrentAssets().stream().map(Asset::clone).collect(Collectors.toMap(Asset::getAsset, Function.identity()));
        this.lockedAssets = incomeState.getLockedAssets().stream().map(LockedAsset::clone).collect(Collectors.toMap(Asset::getAsset, Function.identity()));
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

}