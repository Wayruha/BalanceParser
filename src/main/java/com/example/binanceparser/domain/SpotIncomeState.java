package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import com.example.binanceparser.domain.events.AccountPositionUpdateEvent;
import com.example.binanceparser.domain.events.BalanceUpdateEvent;
import com.example.binanceparser.domain.events.OrderTradeUpdateEvent;
import lombok.*;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static com.example.binanceparser.domain.TransactionType.*;
import static java.math.BigDecimal.ZERO;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SpotIncomeState extends BalanceState {
    private Set<Asset> currentAssets;
    private Set<LockedAsset> lockedAssets;
    private List<Transaction> transactions;
    private List<TransactionX> TXs;

    public SpotIncomeState(LocalDateTime dateTime) {
        super(ZERO, dateTime);
        currentAssets = new LinkedHashSet<>(List.of(new Asset(VIRTUAL_USD, ZERO)));
        lockedAssets = new LinkedHashSet<>();
        transactions = new ArrayList<>();
        TXs = new ArrayList<>();
    }

    public SpotIncomeState(LocalDateTime dateTime, SpotIncomeState incomeState) {
        super(incomeState.getBalanceState(), dateTime);
        this.currentAssets = incomeState.getCurrentAssets().stream().map(Asset::clone).collect(Collectors.toCollection(LinkedHashSet::new));
        this.lockedAssets = incomeState.getLockedAssets().stream().map(LockedAsset::clone).collect(Collectors.toCollection(LinkedHashSet::new));
        this.transactions = new ArrayList<>();
        TXs = new ArrayList<>();
    }

    public BigDecimal calculateVirtualUSDBalance() {
        BigDecimal virtualBalance = ZERO;
        // works for quoteAsset = USD
        for (LockedAsset lockedAsset : lockedAssets) {
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
        return lockedAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst();
    }

    public LockedAsset addLockedAssetIfNotExist(String assetName) {
        final Optional<LockedAsset> lockedAsset = findLockedAsset(assetName);
        if (lockedAsset.isPresent())
            return lockedAsset.get();

        final LockedAsset newAsset = LockedAsset.empty(assetName);
        lockedAssets.add(newAsset);
        return newAsset;
    }

    //TODO remove and use optional-style instead
    @Deprecated
    public Asset findAsset(String assetName) {
        return findAssetOpt(assetName).orElse(null);
    }

    public Optional<Asset> findAssetOpt(String assetName) {
        return currentAssets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst();
    }

    public Asset addAssetIfNotExist(String assetName) {
        final Asset asset = findAsset(assetName);
        if (asset != null)
            return asset;

        final Asset newAsset = new Asset(assetName, ZERO);
        currentAssets.add(newAsset);
        return newAsset;
    }

    public void removeAssetStateIfEmpty(String... assetNames) {
        for (String assetName : assetNames) {
            final Optional<LockedAsset> opt = findLockedAsset(assetName);
            if (opt.isEmpty()) continue;
            if (opt.get().getBalance().signum() == 0) {
                lockedAssets.remove(opt.get());
            }
        }
    }

   /* public TransactionType getLastTransactionType() {
        return transactions.size() != 0 ? transactions.get(transactions.size() - 1).getTransactionType() : null;
    }*/

    public void processBalanceUpdate(BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final BigDecimal balanceDelta = balanceEvent.getBalanceDelta();

        if (balanceDelta.signum() <= 0) {
            handleWithdraw(balanceEvent, accEvent);
        } else {
            handleDeposit(balanceEvent, accEvent);
        }
        findAsset(VIRTUAL_USD).setBalance(calculateVirtualUSDBalance());
    }

    public void processOrder(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        if (orderEvent.getSide().equals("BUY") && isStableCoin(orderEvent.getQuoteAsset())) {
            handleBuy(orderEvent, accEvent);
        } else if (orderEvent.getSide().equals("BUY") && isStableCoin(orderEvent.getQuoteAsset())) {
            handleSell(orderEvent, accEvent);
        } else {
            handleConvertOperation(orderEvent, accEvent);
        }
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

    /**
     * Цей метод враховує тільки "легальну" частину ассета за який ми щось купляємо.
     * Віповідно, ми додаємо в lockAssets тільки ту частину купляємої монети (baseAsset), яку можемо оплатити "легальними" коштами.
     * Решта монети не враховується
     * <p>
     * Протестувати зробивши ордер на більше коштів ніж у нас є в "легальному" балансі
     */
    private void handleBuySaveOnlyKnownProportionally(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent, LockedAsset lockedAsset) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
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

        transactions.add(new Transaction(BUY, baseAssetName, quoteAssetName, orderEvent.getTradeDelta(),
                orderPrice, ZERO));

        updateAssetsBalance(orderEvent, accEvent);
        final Optional<Asset> baseAsset = findAssetOpt(baseAssetName);
        final Optional<Asset> quoteAsset = findAssetOpt(quoteAssetName);
        Asset2 base = Asset2.builder()
                .assetName(baseAssetName)
                .txQty(orderEvent.getActualQty())
                .fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedAsset.getBalance())
                .stableValue(lockedAsset.getStableValue())
                .build();
        Asset2 quote = Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteOrderQty)
                .fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedQuoteAsset.getBalance())
                .stableValue(lockedQuoteAsset.getStableValue())
                .build();
        final TradeTX tx = TradeTX.buyTx(base, quote);
        TXs.add(tx);
    }

    public void handleConvertOperation(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        throw new NotImplementedException("not yet");
    }

    //TODO ми платимо комісію за операцію, мабуть потрібно тут її вказувати в income
    /**
     * просто збільшуємо баланс/stableValue LockedAsset'у якщо він вже існує.
     */
    public void handleBuy(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final BigDecimal baseQty = orderEvent.getActualQty();
        final BigDecimal quoteQty = orderEvent.getQuoteAssetQty();

        updateAssetsBalance(orderEvent, accEvent);

        final LockedAsset baseLocked = addLockedAssetIfNotExist(baseAssetName);
        baseLocked.addBalance(baseQty, quoteQty);

        final Optional<Asset> baseAsset = findAssetOpt(baseAssetName);
        final Optional<Asset> quoteAsset = findAssetOpt(quoteAssetName);
        final Optional<LockedAsset> quoteLocked = findLockedAsset(quoteAssetName);
        Asset2 base = Asset2.builder()
                .assetName(baseAssetName)
                .txQty(baseQty)
                .fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(baseLocked.getBalance())
                .stableValue(baseLocked.getStableValue())
                .build();
        Asset2 quote = Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteQty)
                .fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(quoteLocked.map(LockedAsset::getBalance).orElse(ZERO))
                .stableValue(quoteLocked.map(LockedAsset::getStableValue).orElse(ZERO))
                .build();

        TXs.add(TradeTX.buyTx(base, quote));
    }

    /**
     * Якщо купили base монету (XRP) за іншу quote монету (BTC), то додаємо всю кількість baseQty в lockAsset
     * stableCoinValue встановлюємо пропорційно до кількості використаного quoteAsset.
     * new stableCoinValue = existing stableCoinValue + orderQuoteAssetQty/lockedQuoteAssetQty * quoteAssetStableCoinValue
     * Побічні ефекти:
     * - якщо ми маємо "легальний" BTC і "нелегальний" BTC, то ми фактично оперуємо "легальним" і "нелегальним" по ціні легального.
     * ПРОТЕ ми не хочемо встановлювати ніякої ціни на "нелегальний" BTC
     */
    private void handleBuySaveWholeQuoteAssetBalance(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent, LockedAsset lockedAsset) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
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
        transactions.add(new Transaction(BUY, baseAssetName, quoteAssetName, orderEvent.getTradeDelta(),
                orderPrice, ZERO));

        updateAssetsBalance(orderEvent, accEvent);
        //TODO should be done AFTER balances are updated
        final Optional<Asset> baseAsset = findAssetOpt(baseAssetName);
        final Optional<Asset> quoteAsset = findAssetOpt(quoteAssetName);
        Asset2 base = Asset2.builder()
                .assetName(baseAssetName)
                .txQty(orderEvent.getActualQty())
                .fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedAsset.getBalance())
                .stableValue(lockedAsset.getStableValue())
                .build();
        Asset2 quote = Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteOrderQty)
                .fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedQuoteAsset.getBalance())
                .stableValue(lockedQuoteAsset.getStableValue())
                .build();
        TXs.add(TradeTX.buyTx(base, quote));
    }

    private void handleDeposit(BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String assetName = balanceEvent.getBalances();
        final BigDecimal assetQty = balanceEvent.getBalanceDelta();

        updateAssetsBalance(balanceEvent, accEvent);

        final Optional<Asset> existingAsset = findAssetOpt(assetName);
        final Optional<LockedAsset> lockedAsset = findLockedAsset(assetName);
        Asset2 txAsset = Asset2.builder()
                .assetName(assetName)
                .txQty(assetQty)
                .fullBalance(existingAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedAsset.map(LockedAsset::getBalance).orElse(ZERO))
                .stableValue(lockedAsset.map(LockedAsset::getStableValue).orElse(ZERO))
                .build();

        final BigDecimal transactionStableValue = STABLECOIN_RATE.getOrDefault(assetName, ZERO).multiply(assetQty);
        TXs.add(UpdateTX.depositTx(txAsset, transactionStableValue));
    }

    //we can avoid using LockedAsset
   /* private void handleWithdraw2(BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String assetName = balanceEvent.getBalances();
        final BigDecimal assetDelta = balanceEvent.getBalanceDelta();
        final Optional<LockedAsset> optLocked = findLockedAsset(assetName);
        final Optional<Asset> existingAsset = findAssetOpt(assetName);

        final BigDecimal prevFullAssetBalance = existingAsset.map(Asset::getBalance).orElse(ZERO).add(assetDelta);

        // імітуємо ніби ми спочатку використовуємо для зняття "нелегальні" кошти а потім вже "valuable" (locked)
        final BigDecimal valuableAssetBalance = optLocked.map(Asset::getBalance).orElse(ZERO);
        final BigDecimal nonValuableAssetBalance = prevFullAssetBalance.subtract(valuableAssetBalance);
        final BigDecimal withdrawValuableQty = assetDelta.subtract(nonValuableAssetBalance).min(ZERO);
        final BigDecimal withdrawValuableProportion = valuableAssetBalance.signum() > 0 ? withdrawValuableQty.divide(valuableAssetBalance) : ZERO;//error


        //---------
        BigDecimal stableValueLost = ZERO;
        if (optLocked.isPresent()) {
            final LockedAsset lockedAsset = optLocked.get();
            final BigDecimal absAssetDeltaCapped = lockedAsset.getBalance().min(assetDelta.abs());
            //TODO lockedAsset.getBalance can be null
            final BigDecimal usedAssetFraction = lockedAsset.getBalance() != null ? absAssetDeltaCapped.divide(lockedAsset.getBalance(), MATH_CONTEXT) : ZERO;
            stableValueLost = usedAssetFraction.multiply(lockedAsset.getStableValue());
            lockedAsset.setStableValue(lockedAsset.getStableValue().subtract(stableValueLost));
            lockedAsset.setBalance(lockedAsset.getBalance().subtract(absAssetDeltaCapped));// unlock
//        setBalanceState(getBalanceState().add(transactionIncome));
        }
        transactions.add(new Transaction(WITHDRAW, assetName, "", assetDelta, null, null));
        //TODO is assetDelta positive?
        Asset2 txAsset = Asset2.builder()
                .assetName(assetName)
                .txQty(assetDelta)
                .fullBalance(existingAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(optLocked.map(LockedAsset::getBalance).orElse(ZERO))
                .stableValue(optLocked.map(LockedAsset::getStableValue).orElse(ZERO))
                .build();
        TXs.add(UpdateTX.withdrawTx(txAsset, stableValueLost));
    }*/

    public void handleWithdraw(BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String assetName = balanceEvent.getBalances();
        final BigDecimal assetDelta = balanceEvent.getBalanceDelta();
        final Asset existingAsset = findAssetOpt(assetName).get(); //not null because we CAN withdraw it
        final Optional<LockedAsset> optLocked = findLockedAsset(assetName);

        final BigDecimal valuableAssetBalance = optLocked.map(Asset::getBalance).orElse(ZERO);
        final BigDecimal nonValuableAssetBalance = existingAsset.getBalance().subtract(valuableAssetBalance);
        final BigDecimal withdrawValuableQty = assetDelta.subtract(nonValuableAssetBalance).max(ZERO);

        BigDecimal stableValueLost = ZERO;
        if (optLocked.isPresent()) {
            final BigDecimal stableValueBefore = optLocked.map(LockedAsset::getStableValue).orElse(ZERO);
            final LockedAsset locked = optLocked.get();
            locked.deductBalance(withdrawValuableQty);
            stableValueLost = stableValueBefore.subtract(locked.getStableValue());
        }

        updateAssetsBalance(balanceEvent, accEvent);

        Asset2 txAsset = Asset2.builder()
                .assetName(assetName)
                .txQty(assetDelta)
                .fullBalance(existingAsset.getBalance())
                .valuableBalance(optLocked.map(LockedAsset::getBalance).orElse(ZERO))
                .stableValue(optLocked.map(LockedAsset::getStableValue).orElse(ZERO))
                .build();
        TXs.add(UpdateTX.withdrawTx(txAsset, stableValueLost));
    }

    //    private void handleSell(OrderTradeUpdateEvent event, String baseAssetName, BigDecimal assetDelta, BigDecimal transactionPrice, LockedAsset lockedAsset) {
   /* private void handleSell2(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent, LockedAsset lockedAsset) {
        final BigDecimal assetDelta = orderEvent.getTradeDelta();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final BigDecimal transactionPrice = orderEvent.getPriceIncludingCommission();
        final BigDecimal quoteAssetQty = orderEvent.getQuoteAssetQty();
        final String baseAssetName = orderEvent.getBaseAsset();


        BigDecimal maxAssetTrackedAmount = lockedAsset.getBalance().compareTo(assetDelta.abs()) >= 0
                ? assetDelta : lockedAsset.getBalance().negate();
        BigDecimal transactionIncome = maxAssetTrackedAmount.abs().multiply(transactionPrice)// what we got when sold asset
                .subtract(maxAssetTrackedAmount.abs().multiply(lockedAsset.getAverageQuotePrice()));// what we spent
        // when bought  asset
        setBalanceState(getBalanceState().add(transactionIncome));
        lockedAsset.setBalance(lockedAsset.getBalance().subtract(maxAssetTrackedAmount.abs()));// unlock asset

        transactions.add(new Transaction(SELL, baseAssetName, lockedAsset.getQuoteAsset(),
                assetDelta, transactionPrice, transactionIncome));

        final Optional<Asset> baseAsset = findAssetOpt(baseAssetName);
        final Optional<Asset> quoteAsset = findAssetOpt(quoteAssetName);
        final Optional<LockedAsset> quoteAssetLocked = findLockedAsset(quoteAssetName);
        Asset2 base = Asset2.builder()
                .assetName(baseAssetName)
                .txQty(orderEvent.getActualQty())
                .fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedAsset.getBalance())
                .stableValue(lockedAsset.getStableValue())
                .build();
        Asset2 quote = Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteAssetQty)
                .fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(quoteAssetLocked.map(LockedAsset::getBalance).orElse(ZERO))
                .stableValue(quoteAssetLocked.map(LockedAsset::getStableValue).orElse(ZERO))
                .build();
        TXs.add(TradeTX.sellTx(base, quote, null));
    }*/

    /**
     * if baseQty > lockedQty then we calculate the profit using only part of baseAsset (since other part comes from unknown source
     * quoteAssetEarnedWithValuableFunds = quoteAssetQty * (lockedQty / baseQty)
     * lockedQty.balance -= baseQtyCapped (locked part of asset)
     * lockedQty.stableValue -= {proportionally to balance change in prev. line}
     * income = {what we can got selling 'valuable' asset - stableValue of that asset}
     */
    private void handleSell(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final BigDecimal baseQty = orderEvent.getActualQty();
        final BigDecimal quoteAssetQty = orderEvent.getQuoteAssetQty();

        final Optional<LockedAsset> baseAssetLocked = findLockedAsset(baseAssetName);

        final BigDecimal lockedQty = baseAssetLocked.map(Asset::getBalance).orElse(ZERO);
        final BigDecimal baseQtyCapped = baseQty.min(lockedQty);

        BigDecimal stableValueUnlocked = ZERO;
        BigDecimal quoteAssetEarnedWithValuableFunds = quoteAssetQty;
        if (baseQty.compareTo(lockedQty) > 0) {
            quoteAssetEarnedWithValuableFunds = quoteAssetQty.multiply(lockedQty.divide(baseQty, MATH_CONTEXT));
        }

        //if there is no locked (or 'valuable') qty then user does not profit from such operation
        if (baseAssetLocked.isPresent()) {
            final LockedAsset locked = baseAssetLocked.get();
            final BigDecimal stableValueBefore = locked.getStableValue();
            locked.deductBalance(baseQtyCapped);
            stableValueUnlocked = stableValueBefore.subtract(locked.getStableValue());
        }
        final BigDecimal income = quoteAssetEarnedWithValuableFunds.subtract(stableValueUnlocked);

        updateAssetsBalance(orderEvent, accEvent);

        final Asset baseAsset = findAssetOpt(baseAssetName).get();  //not null because we CAN sell it
        final Optional<Asset> quoteAsset = findAssetOpt(quoteAssetName);
        Asset2 base = Asset2.builder()
                .assetName(baseAssetName)
                .txQty(baseQty)
                .fullBalance(baseAsset.getBalance())
                .valuableBalance(baseAssetLocked.map(LockedAsset::getBalance).orElse(ZERO))
                .stableValue(baseAssetLocked.map(LockedAsset::getStableValue).orElse(ZERO))
                .build();
        final BigDecimal quoteAssetBalance = quoteAsset.map(Asset::getBalance).orElse(ZERO); // quoteAsset is always stablecoin in this method
        Asset2 quote = Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteAssetQty)
                .fullBalance(quoteAssetBalance)
                .valuableBalance(quoteAssetBalance)
                .stableValue(quoteAssetBalance)
                .build();
        TXs.add(TradeTX.sellTx(base, quote, income));
    }

    private void updateAssetsBalance(BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAsset = balanceEvent.getBalances();
        final LocalDateTime dateTime = balanceEvent.getDateTime();
        updateAssetsBalance(accEvent, baseAsset, dateTime);
    }

    private void updateAssetsBalance(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAsset = orderEvent.getBaseAsset();
        final LocalDateTime dateTime = orderEvent.getDateTime();
        updateAssetsBalance(accEvent, baseAsset, dateTime);
    }

    private void updateAssetsBalance(AccountPositionUpdateEvent accEvent, String baseAsset, LocalDateTime dateTime) {
        final AssetMetadata assetMetadata = AssetMetadata.builder()
                .dateOfLastTransaction(dateTime).quoteAsset("")
                .priceOfLastTrade(ZERO).build();
        final List<Asset> assetsInvolved = accEvent.getBalances().stream().map(asset ->
                Asset.builder()
                        .asset(asset.getAsset())
                        .balance(asset.getFree().add(asset.getLocked()))
                        .assetMetadata(asset.getAsset().equals(baseAsset) ? assetMetadata : null)
                        .build()
        ).collect(Collectors.toList());
        updateAssetsBalance(assetsInvolved);
    }

    public void updateAssetsBalance(List<Asset> updatedAssets) {
        updatedAssets.forEach(updatedAsset -> {
            final Optional<Asset> assetOpt = findAssetOpt(updatedAsset.getAsset());
            assetOpt.ifPresentOrElse(asset -> asset.setBalance(updatedAsset.getBalance()),
                    () -> currentAssets.add(updatedAsset));
            removeAssetStateIfEmpty(updatedAsset.getAsset());
        });
        // copying current stableCoins to AssetStates set
        this.currentAssets.stream()
                .filter(asset -> STABLECOIN_RATE.containsKey(asset.getAsset()))
                .forEach(stableCoin -> {
                    final LockedAsset lockedAsset = addLockedAssetIfNotExist(stableCoin.getAsset());
                    lockedAsset.setBalance(stableCoin.getBalance());
                    lockedAsset.setStableValue(stableCoin.getBalance().multiply(STABLECOIN_RATE.get(stableCoin.getAsset())));
                });
    }

    @Data
    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
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
            stableValue = stableValue.subtract(stableValue.multiply(qtyToBalanceFraction));
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