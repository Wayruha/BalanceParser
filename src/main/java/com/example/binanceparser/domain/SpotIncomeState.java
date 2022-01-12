package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static com.example.binanceparser.domain.TransactionType.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SpotIncomeState extends BalanceState {
    private Set<Asset> currentAssets;
    //TODO якщо в lockedAssetState лежить ціна 0 - що це означає? мабуть ми не хочемо щоб такі асети лежали у нас
    private Set<LockedAsset> lockedAssets;
    private List<Transaction> transactions;

    public SpotIncomeState(LocalDateTime dateTime) {
        super(BigDecimal.ZERO, dateTime);
        currentAssets = new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, BigDecimal.ZERO)));
        lockedAssets = new LinkedHashSet<>();
        transactions = new ArrayList<>();
    }

    public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
        super(incomeState.getBalanceState(), dateTime);
        this.currentAssets = incomeState.getCurrentAssets().stream().map(Asset::clone).collect(Collectors.toCollection(LinkedHashSet::new));
        this.lockedAssets = incomeState.getLockedAssets().stream().map(LockedAsset::clone).collect(Collectors.toCollection(LinkedHashSet::new));
        this.transactions = new ArrayList<>();
    }

    public BigDecimal calculateVirtualUSDBalance() {
        BigDecimal virtualBalance = BigDecimal.ZERO;
        // works for quoteAsset = USD
        for (LockedAsset lockedAsset : lockedAssets) {
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
        return lockedAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst();
    }

    public LockedAsset addLockedAssetIfNotExist(String assetName) {
        final Optional<LockedAsset> lockedAsset = findLockedAsset(assetName);
        if (lockedAsset.isPresent())
            return lockedAsset.get();

        final LockedAsset newAsset = new LockedAsset(assetName, BigDecimal.ZERO, null, BigDecimal.ZERO);
        lockedAssets.add(newAsset);
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
            lockedAssets.remove(opt.get());
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

   /* public TransactionType getLastTransactionType() {
        return transactions.size() != 0 ? transactions.get(transactions.size() - 1).getTransactionType() : null;
    }*/

    public void processBalanceUpdate(BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAsset = balanceEvent.getBalances();
        final BigDecimal balanceDelta = balanceEvent.getBalanceDelta();

        if(balanceDelta.signum() <= 0){
            handleWithdraw(baseAsset, balanceDelta);
        }else {
            handleDeposit(baseAsset, balanceDelta);
        }

        // can be compacted
        final AssetMetadata assetMetadata = AssetMetadata.builder()
                .dateOfLastTransaction(balanceEvent.getDateTime()).quoteAsset("")
                .priceOfLastTrade(BigDecimal.ZERO).build();
        final List<Asset> assetsInvolved = accEvent.getBalances().stream().map(asset ->
                Asset.builder()
                        .asset(asset.getAsset())
                        .balance(asset.getFree().add(asset.getLocked()))
                        .assetMetadata(asset.getAsset().equals(baseAsset) ? assetMetadata : null)
                        .build()
        ).collect(Collectors.toList());
        updateAssetBalance(assetsInvolved);

        findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());
    }

    // TODO remove!
    public void processOrderDetails(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice) {
//        final LockedAsset lockedAsset = addLockedAssetIfNotExist(assetName);
       /* if (assetDelta.compareTo(BigDecimal.ZERO) <= 0) {
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

    public void processOrder(OrderTradeUpdateEvent orderEvent) {
        final String baseAsset = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final LockedAsset lockedAsset = addLockedAssetIfNotExist(baseAsset);

        if (orderEvent.getSide().equals("BUY")) {
//            handleBuySaveWholeQuoteAssetBalance(orderEvent, baseAsset, quoteAssetName, lockedAsset);
            handleBuySaveOnlyKnownProportionally(orderEvent, baseAsset, quoteAssetName, lockedAsset);
        } else {
            handleSell(baseAsset, orderEvent.getTradeDelta(), orderEvent.getPriceIncludingCommission(), lockedAsset);
        }
        findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());
    }

    /**
     * Цей метод враховує тільки "легальну" частину ассета за який ми щось купляємо.
     * Віповідно, ми додаємо в lockAssets тільки ту частину купляємої монети (baseAsset), яку можемо оплатити "легальними" коштами.
     * Решта монети не враховується
     * <p>
     * Протестувати зробивши ордер на більше коштів ніж у нас є в "легальному" балансі
     */
    private void handleBuySaveOnlyKnownProportionally(OrderTradeUpdateEvent orderEvent, String baseAsset, String quoteAssetName, LockedAsset lockedAsset) {
        final Optional<LockedAsset> optLockedQuoteAsset = findLockedAsset(quoteAssetName);
        if (optLockedQuoteAsset.isEmpty()) return; // нічого не оновлюємо

        final BigDecimal orderQty = orderEvent.getActualQty();
        final BigDecimal orderPrice = orderEvent.getPriceIncludingCommission();
        final BigDecimal quoteOrderQty = orderEvent.getQuoteAssetQty();
        final LockedAsset lockedQuoteAsset = optLockedQuoteAsset.get();

        // ми не можемо додати "легальну" частину більшу ніж ми можемо покрити "легальними" коштами
        final BigDecimal cappedQuoteAssetQty = quoteOrderQty.min(lockedQuoteAsset.getBalance());

        final BigDecimal proportionOfAvailableQuoteAssetUsed = cappedQuoteAssetQty.divide(lockedQuoteAsset.getBalance(), MATH_CONTEXT);
        final BigDecimal baseAssetStableValue = proportionOfAvailableQuoteAssetUsed.multiply(lockedQuoteAsset.getStableValue());
        lockedAsset.setStableValue(lockedAsset.getStableValue().add(baseAssetStableValue));

        //додаємо до залоченого стану ЧАСТИНУ купленої монети, якщо ми використали більше ресурсу ніж маємо "легально"
        // qty = min(lockedQuoteQty/orderQuoteQty, 1) * baseOrderQty
        final BigDecimal proportionalBaseQty = cappedQuoteAssetQty.divide(quoteOrderQty, MATH_CONTEXT).multiply(orderQty);
        final BigDecimal newAssetQty = lockedAsset.getBalance().add(proportionalBaseQty);
        lockedAsset.setBalance(newAssetQty);
        lockedAsset.setQuoteAsset(quoteAssetName);
        lockedAsset.setAverageQuotePrice(orderQty.divide(quoteOrderQty, MATH_CONTEXT));

        transactions.add(new Transaction(BUY, baseAsset, quoteAssetName, orderEvent.getTradeDelta(),
                orderPrice, BigDecimal.ZERO));
    }

    /**
     * Якщо купили base монету (XRP) за іншу quote монету (BTC), то додаємо всю кількість baseQty в lockAsset
     * stableCoinValue встановлюємо пропорційно до кількості використаного quoteAsset.
     * new stableCoinValue = existing stableCoinValue + orderQuoteAssetQty/lockedQuoteAssetQty * quoteAssetStableCoinValue
     * Побічні ефекти:
     * - якщо ми маємо "легальний" BTC і "нелегальний" BTC, то ми фактично оперуємо "легальним" і "нелегальним" по ціні легального.
     * ПРОТЕ ми не хочемо встановлювати ніякої ціни на "нелегальний" BTC
     */
    private void handleBuySaveWholeQuoteAssetBalance(OrderTradeUpdateEvent orderEvent, String baseAsset, String quoteAssetName, LockedAsset lockedAsset) {
        final Optional<LockedAsset> quoteAssetLockedState = findLockedAsset(quoteAssetName);
        if (quoteAssetLockedState.isEmpty()) return; // не оновлюємо нічого. краще навіть видалити те що додавали

        final BigDecimal orderQty = orderEvent.getActualQty();
        final BigDecimal orderPrice = orderEvent.getPriceIncludingCommission();
        final BigDecimal quoteOrderQty = orderEvent.getQuoteAssetQty();

        //update locked asset's price and qty
        final BigDecimal newAssetQty = lockedAsset.getBalance().add(orderEvent.getTradeDelta());
        lockedAsset.setQuoteAsset(quoteAssetName);
        lockedAsset.setBalance(newAssetQty);
        lockedAsset.setAverageQuotePrice(orderQty.divide(quoteOrderQty, MATH_CONTEXT));

        final LockedAsset lockedQuoteAsset = quoteAssetLockedState.get();

        final BigDecimal proportionOfQuoteAssetUsed = quoteOrderQty.divide(lockedQuoteAsset.getBalance(), MATH_CONTEXT);
        final BigDecimal quoteAssetStableCoinValue = proportionOfQuoteAssetUsed.multiply(lockedQuoteAsset.getStableValue());
        //все заради чого ми оце робимо - ДОДАТИ монеті stablecoinValue
        lockedAsset.setStableValue(lockedAsset.getStableValue().add(quoteAssetStableCoinValue));
        transactions.add(new Transaction(BUY, baseAsset, quoteAssetName, orderEvent.getTradeDelta(),
                orderPrice, BigDecimal.ZERO));
    }

    private void handleDeposit(String assetName, BigDecimal assetDelta) {
        transactions.add(new Transaction(DEPOSIT, assetName, "", assetDelta, null,
                BigDecimal.ZERO));
    }

    //we can avoid using LockedAsset
    private void handleWithdraw(String assetName, BigDecimal assetDelta) {
        final Optional<LockedAsset> optLocked = findLockedAsset(assetName);
        if(optLocked.isEmpty()) {
            transactions.add(new Transaction(WITHDRAW, assetName, "", assetDelta, null, null));
            return;
        }
        final LockedAsset lockedAsset = optLocked.get();
        final BigDecimal absAssetDeltaCapped = lockedAsset.getBalance().min(assetDelta.abs());
        final BigDecimal usedAssetFraction = absAssetDeltaCapped.divide(lockedAsset.getBalance(), MATH_CONTEXT);
        final BigDecimal stableValueLost = usedAssetFraction.multiply(lockedAsset.getStableValue());
        lockedAsset.setStableValue(lockedAsset.getStableValue().subtract(stableValueLost));
        lockedAsset.setBalance(lockedAsset.getBalance().subtract(absAssetDeltaCapped));// unlock
//        setBalanceState(getBalanceState().add(transactionIncome));
    }

    private void handleSell(String assetName, BigDecimal assetDelta, BigDecimal transactionPrice, LockedAsset lockedAsset) {
        BigDecimal maxAssetTrackedAmount = lockedAsset.getBalance().compareTo(assetDelta.abs()) >= 0
                ? assetDelta
                : lockedAsset.getBalance().negate();
        BigDecimal transactionIncome = maxAssetTrackedAmount.abs().multiply(transactionPrice)// what we got when sold asset
                .subtract(maxAssetTrackedAmount.abs().multiply(lockedAsset.getAverageQuotePrice()));// what we spent
        // when bought  asset
        setBalanceState(getBalanceState().add(transactionIncome));
        lockedAsset.setBalance(lockedAsset.getBalance().subtract(maxAssetTrackedAmount.abs()));// unlock asset
        transactions.add(new Transaction(SELL, assetName, lockedAsset.getQuoteAsset(),
                assetDelta, transactionPrice, transactionIncome));
    }

    @Data
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class LockedAsset extends Asset {
        private String quoteAsset;
        // price of this asset relative to quoteAsset. E.g., relativeAsset = USD
        private BigDecimal averageQuotePrice;
        private BigDecimal stableValue;

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