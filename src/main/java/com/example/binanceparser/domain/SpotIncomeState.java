package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        this.currentAssets = incomeState.getCurrentAssets().stream().map(Asset::clone).collect(Collectors.toCollection(LinkedHashSet::new));
        this.lockedLockedAssets = incomeState.getLockedLockedAssets().stream().map(LockedAsset::clone).collect(Collectors.toCollection(LinkedHashSet::new));
        this.transactions = new ArrayList<>();
    }

    public BigDecimal calculateVirtualUSDBalance() {
        BigDecimal virtualBalance = BigDecimal.ZERO;
        // works for quoteAsset = USD
        for (LockedAsset lockedAsset : lockedLockedAssets) {
            virtualBalance = virtualBalance
                    .add(lockedAsset.getBalance().multiply(lockedAsset.getAverageQuotePrice()));
        }
        return virtualBalance.round(Constants.MATH_CONTEXT);
    }

    public BigDecimal calculateVirtualUSDBalance(String asset) {
        if (asset.equals(VIRTUAL_USD)) {
            return calculateVirtualUSDBalance();
        }
        // works for quoteAsset = USD
        final Optional<LockedAsset> lockedAsset = findLockedAsset(asset);
        return lockedAsset
                .map(a -> a.getBalance().multiply(a.getAverageQuotePrice()).round(Constants.MATH_CONTEXT))
                .orElse(BigDecimal.ZERO);
    }

  /*  public LockedAsset findLockedAsset(String assetName) {
        return lockedLockedAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
    }*/

    public Optional<LockedAsset> findLockedAsset(String assetName) {
        return lockedLockedAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst();
    }

    public LockedAsset addLockedAssetIfNotExist(String assetName) {
        final Optional<LockedAsset> lockedAsset = findLockedAsset(assetName);
        if (lockedAsset.isPresent())
            return lockedAsset.get();

        final LockedAsset newAsset = new LockedAsset(assetName, BigDecimal.ZERO, null, null);
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
        final Optional<LockedAsset> opt = findLockedAsset(assetName);
        if (opt.isEmpty()) return;
        final LockedAsset lockedAsset = opt.get();
        if (lockedAsset.getBalance().signum() == 0) {
            lockedLockedAssets.remove(opt.get());
        }
    }

    public void updateAssetBalance(List<Asset> updatedAssets) {
        updatedAssets.forEach(updatedAsset -> {
            currentAssets.removeIf(asset -> asset.getAsset().equals(updatedAsset.getAsset()));
            currentAssets.add(updatedAsset);
        });

        // copying current stableCoins to AssetStates set
        this.currentAssets.stream()
                .filter(asset -> STABLECOIN_RATE.containsKey(asset.getAsset()))
                .forEach(stableCoin -> {
                    final LockedAsset lockedAsset = addLockedAssetIfNotExist(stableCoin.getAsset());
                    lockedAsset.setBalance(stableCoin.getBalance());
                    lockedAsset.setAverageQuotePrice(STABLECOIN_RATE.get(stableCoin.getAsset()));
                });
    }

    public TransactionType getLastTransactionType() {
        return transactions.size() != 0 ? transactions.get(transactions.size() - 1).getTransactionType() : null;
    }

    public void processBalanceUpdate(String assetName, BigDecimal assetDelta, TransactionType transaction) {

    }

    // TODO refactor
    public void processOrderDetails(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
        final LockedAsset lockedAsset = addLockedAssetIfNotExist(assetName);
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
            //TODO чи значить це, що стан монети (напр. ЮСДТ) в ассетах зміниться, а в локедАссетах ні? В асетах буде 500 USDT а в локед = 400
            // else DEPOSIT operation (we do not add deposit to locked assets)
            handleDeposit(assetName, assetDelta, transactionPrice);
        }

        findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());
    }

    private void handleDeposit(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
        transactions.add(new Transaction(TransactionType.DEPOSIT, assetName, "", assetDelta, transactionPrice,
                BigDecimal.ZERO));
    }

    private void handleBuy(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice, LockedAsset lockedAsset) {
        transactions.add(new Transaction(TransactionType.BUY, assetName, lockedAsset.getQuoteAsset(), assetDelta,
                transactionPrice, BigDecimal.ZERO));
        final BigDecimal transactionQuoteQty = assetDelta.multiply(transactionPrice);
        final BigDecimal newAssetQty = lockedAsset.getBalance().add(assetDelta);
        final BigDecimal newCalculatedPrice = lockedAsset.totalQuoteAssetValue().add(transactionQuoteQty)
                .divide(newAssetQty, Constants.MATH_CONTEXT);
        lockedAsset.setAverageQuotePrice(newCalculatedPrice);
        lockedAsset.setBalance(newAssetQty);
    }

    private void handleWithdraw(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice, LockedAsset lockedAsset) {
        transactions.add(new Transaction(TransactionType.WITHDRAW_IN_PROCESS, assetName, "", assetDelta,
                transactionPrice, null));
        processOrderDetails(assetName, assetDelta, lockedAsset.getAverageQuotePrice());
        transactions.set(transactions.size() - 1, new Transaction(TransactionType.WITHDRAW, assetName, "",
                assetDelta, transactionPrice, transactions.get(transactions.size() - 1).getIncome()));
    }

    private void handleSell(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice, LockedAsset lockedAsset) {
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
                .subtract(maxAssetTrackedAmount.abs().multiply(lockedAsset.getAverageQuotePrice()));// what we spent
        // when bought
        // asset
        transaction.setIncome(transactionIncome);
        setBalanceState(getBalanceState().add(transactionIncome));
        lockedAsset.setBalance(lockedAsset.getBalance().subtract(maxAssetTrackedAmount.abs()));// unlock
        // asset
    }

    @Data
    @ToString(callSuper = true)
    public static class LockedAsset extends Asset {
        private String quoteAsset;
        // price of this asset relative to quoteAsset. E.g., relativeAsset = USD
        private BigDecimal averageQuotePrice;
        private BigDecimal stableCoinValue;


        public LockedAsset(String asset, BigDecimal availableBalance, String quoteAsset, BigDecimal averageQuotePrice) {
            super(asset, availableBalance);
            this.averageQuotePrice = averageQuotePrice;
            this.quoteAsset = quoteAsset;
        }

        public LockedAsset clone() {
            LockedAsset asset = new LockedAsset(super.asset, super.balance, quoteAsset, averageQuotePrice);
            asset.setQuoteAsset(quoteAsset);
            return asset;
        }

        public BigDecimal totalQuoteAssetValue() {
            return averageQuotePrice.multiply(balance);
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