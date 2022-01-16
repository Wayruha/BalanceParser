package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.*;
import com.example.binanceparser.domain.events.*;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.ZERO;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class TestSpotBalanceCalcAlgorithm implements CalculationAlgorithm<SpotIncomeState> {
    private final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;

    public TestSpotBalanceCalcAlgorithm() {
    }

    @Override
    public List<SpotIncomeState> processEvents(List<AbstractEvent> events) {
        return processEvents(events, null);
    }

    @Override
    public List<SpotIncomeState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack) {
        final List<SpotIncomeState> spotIncomeStates = new ArrayList<>();
        for (int i = 0; i < events.size() - 1; i++) {
            final AbstractEvent currentEvent = events.get(i);
            final AbstractEvent nextEvent = events.get(i + 1);

            if ((currentEvent.getEventType() != EventType.ORDER_TRADE_UPDATE
                    && currentEvent.getEventType() != EventType.BALANCE_UPDATE)
                    || nextEvent.getEventType() != EventType.ACCOUNT_POSITION_UPDATE) {
                continue;
            }

            if (ChronoUnit.SECONDS.between(currentEvent.getDateTime(),
                    nextEvent.getDateTime()) > MAX_SECONDS_DELAY_FOR_VALID_EVENTS) {
                continue;
            }

            final SpotIncomeState incomeState = spotIncomeStates.size() == 0
                    ? new SpotIncomeState(currentEvent.getDateTime())
                    : new SpotIncomeState(currentEvent.getDateTime(),
                    spotIncomeStates.get(spotIncomeStates.size() - 1));

            if (currentEvent.getEventType() == EventType.BALANCE_UPDATE) {
                processBalanceUpdate(incomeState, (BalanceUpdateEvent) currentEvent, (AccountPositionUpdateEvent) nextEvent);
                continue;
            }

            final OrderTradeUpdateEvent orderEvent = (OrderTradeUpdateEvent) currentEvent;
            final AccountPositionUpdateEvent accEvent = (AccountPositionUpdateEvent) nextEvent;

            if (!orderEvent.getOrderStatus().equals("FILLED")) {
                continue;
            }

            logTrade(orderEvent, incomeState);

            processOrder(incomeState, orderEvent, accEvent);

            spotIncomeStates.add(incomeState);
        }
        return spotIncomeStates;
    }

    public void processBalanceUpdate(SpotIncomeState state, BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final BigDecimal balanceDelta = balanceEvent.getBalanceDelta();

        if (balanceDelta.signum() <= 0) {
            handleWithdraw(state, balanceEvent, accEvent);
        } else {
            handleDeposit(state, balanceEvent, accEvent);
        }
        state.findAssetOpt(VIRTUAL_USD).get().setBalance(state.calculateVirtualUSDBalance());
    }

    public void processOrder(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        if (orderEvent.getSide().equals("BUY") && isStableCoin(orderEvent.getQuoteAsset())) {
            handleBuy(state, orderEvent, accEvent);
        } else if (orderEvent.getSide().equals("SELL") && isStableCoin(orderEvent.getQuoteAsset())) {
            handleSell(state, orderEvent, accEvent);
        } else {
            handleConvertOperation(state, orderEvent, accEvent);
        }
        state.findAssetOpt(VIRTUAL_USD).get().setBalance(state.calculateVirtualUSDBalance());
    }

    /**
     * Цей метод враховує тільки "легальну" частину ассета за який ми щось купляємо.
     * Віповідно, ми додаємо в lockAssets тільки ту частину купляємої монети (baseAsset), яку можемо оплатити "легальними" коштами.
     * Решта монети не враховується
     * <p>
     * Протестувати зробивши ордер на більше коштів ніж у нас є в "легальному" балансі
     *//*
	private void handleBuySaveOnlyKnownProportionally(OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent, SpotIncomeState.LockedAsset lockedAsset) {
		final String baseAssetName = orderEvent.getBaseAsset();
		final String quoteAssetName = orderEvent.getQuoteAsset();
		final Optional<SpotIncomeState.LockedAsset> optLockedQuoteAsset = findLockedAsset(quoteAssetName);
		if (optLockedQuoteAsset.isEmpty()) return; // нічого не оновлюємо

		final BigDecimal orderQty = orderEvent.getActualQty();
		final BigDecimal orderPrice = orderEvent.getPriceIncludingCommission();
		final BigDecimal quoteOrderQty = orderEvent.getQuoteAssetQty();
		final SpotIncomeState.LockedAsset lockedQuoteAsset = optLockedQuoteAsset.get();

		// ми не можемо додати "легальну" частину більшу ніж ми можемо покрити "легальними" коштами
		final BigDecimal cappedQuoteAssetQty = quoteOrderQty.min(lockedQuoteAsset.getBalance());

		final BigDecimal proportionOfAvailableQuoteAssetUsed = cappedQuoteAssetQty.divide(lockedQuoteAsset.getBalance(), MATH_CONTEXT);
		final BigDecimal baseAssetStableValue = proportionOfAvailableQuoteAssetUsed.multiply(lockedQuoteAsset.getStableValue(), MATH_CONTEXT);
		lockedAsset.setStableValue(lockedAsset.getStableValue().add(baseAssetStableValue));

		//додаємо до залоченого стану ЧАСТИНУ купленої монети, якщо ми використали більше ресурсу ніж маємо "легально"
		// qty = min(lockedQuoteQty/orderQuoteQty, 1) * baseOrderQty
		final BigDecimal proportionalBaseQty = cappedQuoteAssetQty.divide(quoteOrderQty, MATH_CONTEXT).multiply(orderQty, MATH_CONTEXT);
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
	}*/
    public void handleConvertOperation(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        throw new NotImplementedException("not yet");
    }

    //TODO ми платимо комісію за операцію, мабуть потрібно тут її вказувати в income

    /**
     * просто збільшуємо баланс/stableValue LockedAsset'у якщо він вже існує.
     */
    public void handleBuy(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final BigDecimal baseQty = orderEvent.getActualQty();
        final BigDecimal quoteQty = orderEvent.getQuoteAssetQty();

        updateAssetsBalance(state, orderEvent, accEvent);

        final SpotIncomeState.LockedAsset baseLocked = state.addLockedAssetIfNotExist(baseAssetName);
        baseLocked.addBalance(baseQty, quoteQty);

        final Optional<Asset> baseAsset = state.findAssetOpt(baseAssetName);
        final Optional<Asset> quoteAsset = state.findAssetOpt(quoteAssetName);
        final Optional<SpotIncomeState.LockedAsset> quoteLocked = state.findLockedAsset(quoteAssetName);
        TransactionX.Asset2 base = TransactionX.Asset2.builder()
                .assetName(baseAssetName)
                .txQty(baseQty)
                .fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(baseLocked.getBalance())
                .stableValue(baseLocked.getStableValue())
                .build();
        TransactionX.Asset2 quote = TransactionX.Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteQty)
                .fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(quoteLocked.map(SpotIncomeState.LockedAsset::getBalance).orElse(ZERO))
                .stableValue(quoteLocked.map(SpotIncomeState.LockedAsset::getStableValue).orElse(ZERO))
                .build();

        state.getTXs().add(TradeTX.buyTx(base, quote));
    }

    /**
     * Якщо купили base монету (XRP) за іншу quote монету (BTC), то додаємо всю кількість baseQty в lockAsset
     * stableCoinValue встановлюємо пропорційно до кількості використаного quoteAsset.
     * new stableCoinValue = existing stableCoinValue + orderQuoteAssetQty/lockedQuoteAssetQty * quoteAssetStableCoinValue
     * Побічні ефекти:
     * - якщо ми маємо "легальний" BTC і "нелегальний" BTC, то ми фактично оперуємо "легальним" і "нелегальним" по ціні легального.
     * ПРОТЕ ми не хочемо встановлювати ніякої ціни на "нелегальний" BTC
     */
    private void handleBuySaveWholeQuoteAssetBalance(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent, SpotIncomeState.LockedAsset lockedAsset) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final Optional<SpotIncomeState.LockedAsset> quoteAssetLockedState = state.findLockedAsset(quoteAssetName);
        if (quoteAssetLockedState.isEmpty()) return; // не оновлюємо нічого. краще навіть видалити те що додавали

        final BigDecimal orderQty = orderEvent.getActualQty();
        final BigDecimal orderPrice = orderEvent.getPriceIncludingCommission();
        final BigDecimal quoteOrderQty = orderEvent.getQuoteAssetQty();

        //update locked asset's price and qty
        final BigDecimal newAssetQty = lockedAsset.getBalance().add(orderEvent.getTradeDelta());
        lockedAsset.setQuoteAsset(quoteAssetName);
        lockedAsset.setBalance(newAssetQty);
        lockedAsset.setAverageQuotePrice(orderQty.divide(quoteOrderQty, MATH_CONTEXT));

        final SpotIncomeState.LockedAsset lockedQuoteAsset = quoteAssetLockedState.get();

        final BigDecimal proportionOfQuoteAssetUsed = quoteOrderQty.divide(lockedQuoteAsset.getBalance(), MATH_CONTEXT);
        final BigDecimal quoteAssetStableCoinValue = proportionOfQuoteAssetUsed.multiply(lockedQuoteAsset.getStableValue());
        //все заради чого ми оце робимо - ДОДАТИ монеті stablecoinValue
        lockedAsset.setStableValue(lockedAsset.getStableValue().add(quoteAssetStableCoinValue));

        updateAssetsBalance(state, orderEvent, accEvent);
        //TODO should be done AFTER balances are updated
        final Optional<Asset> baseAsset = state.findAssetOpt(baseAssetName);
        final Optional<Asset> quoteAsset = state.findAssetOpt(quoteAssetName);
        TransactionX.Asset2 base = TransactionX.Asset2.builder()
                .assetName(baseAssetName)
                .txQty(orderEvent.getActualQty())
                .fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedAsset.getBalance())
                .stableValue(lockedAsset.getStableValue())
                .build();
        TransactionX.Asset2 quote = TransactionX.Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteOrderQty)
                .fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedQuoteAsset.getBalance())
                .stableValue(lockedQuoteAsset.getStableValue())
                .build();
        state.getTXs().add(TradeTX.buyTx(base, quote));
    }

    private void handleDeposit(SpotIncomeState state, BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String assetName = balanceEvent.getBalances();
        final BigDecimal assetQty = balanceEvent.getBalanceDelta();

        updateAssetsBalance(state, balanceEvent, accEvent);

        final Optional<Asset> existingAsset = state.findAssetOpt(assetName);
        final Optional<SpotIncomeState.LockedAsset> lockedAsset = state.findLockedAsset(assetName);
        TransactionX.Asset2 txAsset = TransactionX.Asset2.builder()
                .assetName(assetName)
                .txQty(assetQty)
                .fullBalance(existingAsset.map(Asset::getBalance).orElse(ZERO))
                .valuableBalance(lockedAsset.map(SpotIncomeState.LockedAsset::getBalance).orElse(ZERO))
                .stableValue(lockedAsset.map(SpotIncomeState.LockedAsset::getStableValue).orElse(ZERO))
                .build();

        final BigDecimal transactionStableValue = STABLECOIN_RATE.getOrDefault(assetName, ZERO).multiply(assetQty);
        state.getTXs().add(UpdateTX.depositTx(txAsset, transactionStableValue));
    }

    public void handleWithdraw(SpotIncomeState state, BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String assetName = balanceEvent.getBalances();
        final BigDecimal qty = balanceEvent.getBalanceDelta().abs();
        final Asset existingAsset = state.findAssetOpt(assetName).get(); //not null because we CAN withdraw it
        final Optional<SpotIncomeState.LockedAsset> optLocked = state.findLockedAsset(assetName);

        final BigDecimal valuableAssetBalance = optLocked.map(Asset::getBalance).orElse(ZERO);
        final BigDecimal nonValuableAssetBalance = existingAsset.getBalance().subtract(valuableAssetBalance);
        final BigDecimal withdrawValuableQty = qty.subtract(nonValuableAssetBalance).max(ZERO);

        BigDecimal stableValueDiff = ZERO;
        if (optLocked.isPresent()) {
            final BigDecimal stableValueBefore = optLocked.map(SpotIncomeState.LockedAsset::getStableValue).orElse(ZERO);
            final SpotIncomeState.LockedAsset locked = optLocked.get();
            locked.deductBalance(withdrawValuableQty);
            stableValueDiff = locked.getStableValue().subtract(stableValueBefore);
        }

        updateAssetsBalance(state, balanceEvent, accEvent);

        TransactionX.Asset2 txAsset = TransactionX.Asset2.builder()
                .assetName(assetName)
                .txQty(qty)
                .fullBalance(existingAsset.getBalance())
                .valuableBalance(optLocked.map(SpotIncomeState.LockedAsset::getBalance).orElse(ZERO))
                .stableValue(optLocked.map(SpotIncomeState.LockedAsset::getStableValue).orElse(ZERO))
                .build();
        state.getTXs().add(UpdateTX.withdrawTx(txAsset, stableValueDiff));
    }

    /**
     * if baseQty > lockedQty then we calculate the profit using only part of baseAsset (since other part comes from unknown source
     * quoteAssetEarnedWithValuableFunds = quoteAssetQty * (lockedQty / baseQty)
     * lockedQty.balance -= baseQtyCapped (locked part of asset)
     * lockedQty.stableValue -= {proportionally to balance change in prev. line}
     * income = {what we can got selling 'valuable' asset - stableValue of that asset}
     */
    private void handleSell(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAssetName = orderEvent.getBaseAsset();
        final String quoteAssetName = orderEvent.getQuoteAsset();
        final BigDecimal baseQty = orderEvent.getActualQty();
        final BigDecimal quoteAssetQty = orderEvent.getQuoteAssetQty();

        final Optional<SpotIncomeState.LockedAsset> baseAssetLocked = state.findLockedAsset(baseAssetName);

        final BigDecimal lockedQty = baseAssetLocked.map(Asset::getBalance).orElse(ZERO);
        final BigDecimal baseQtyCapped = baseQty.min(lockedQty);

        BigDecimal stableValueUnlocked = ZERO;
        BigDecimal quoteAssetEarnedWithValuableFunds = quoteAssetQty;
        if (baseQty.compareTo(lockedQty) > 0) {
            quoteAssetEarnedWithValuableFunds = quoteAssetQty.multiply(lockedQty.divide(baseQty, MATH_CONTEXT), MATH_CONTEXT);
        }

        //if there is no locked (or 'valuable') qty then user does not profit from such operation
        if (baseAssetLocked.isPresent()) {
            final SpotIncomeState.LockedAsset locked = baseAssetLocked.get();
            final BigDecimal stableValueBefore = locked.getStableValue();
            locked.deductBalance(baseQtyCapped);
            stableValueUnlocked = stableValueBefore.subtract(locked.getStableValue());
        }
        final BigDecimal income = quoteAssetEarnedWithValuableFunds.subtract(stableValueUnlocked);

        updateAssetsBalance(state, orderEvent, accEvent);

        final Asset baseAsset = state.findAssetOpt(baseAssetName).get();  //not null because we CAN sell it
        final Optional<Asset> quoteAsset = state.findAssetOpt(quoteAssetName);
        TransactionX.Asset2 base = TransactionX.Asset2.builder()
                .assetName(baseAssetName)
                .txQty(baseQty)
                .fullBalance(baseAsset.getBalance())
                .valuableBalance(baseAssetLocked.map(SpotIncomeState.LockedAsset::getBalance).orElse(ZERO))
                .stableValue(baseAssetLocked.map(SpotIncomeState.LockedAsset::getStableValue).orElse(ZERO))
                .build();
        final BigDecimal quoteAssetBalance = quoteAsset.map(Asset::getBalance).orElse(ZERO); // quoteAsset is always stablecoin in this method
        TransactionX.Asset2 quote = TransactionX.Asset2.builder()
                .assetName(quoteAssetName)
                .txQty(quoteAssetQty)
                .fullBalance(quoteAssetBalance)
                .valuableBalance(quoteAssetBalance)
                .stableValue(quoteAssetBalance)
                .build();
        state.getTXs().add(TradeTX.sellTx(base, quote, income));
    }

    private void updateAssetsBalance(SpotIncomeState state, BalanceUpdateEvent balanceEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAsset = balanceEvent.getBalances();
        final LocalDateTime dateTime = balanceEvent.getDateTime();
        updateAssetsBalance(state, accEvent, baseAsset, dateTime);
    }

    private void updateAssetsBalance(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
        final String baseAsset = orderEvent.getBaseAsset();
        final LocalDateTime dateTime = orderEvent.getDateTime();
        updateAssetsBalance(state, accEvent, baseAsset, dateTime);
    }

    private void updateAssetsBalance(SpotIncomeState state, AccountPositionUpdateEvent accEvent, String baseAsset, LocalDateTime dateTime) {
        final AssetMetadata assetMetadata = AssetMetadata.builder()
                .dateOfLastTransaction(dateTime).build();
        final List<Asset> assetsInvolved = accEvent.getBalances().stream().map(asset ->
                Asset.builder()
                        .asset(asset.getAsset())
                        .balance(asset.getFree().add(asset.getLocked()))
                        .assetMetadata(asset.getAsset().equals(baseAsset) ? assetMetadata : null)
                        .build()
        ).collect(Collectors.toList());
        state.updateAssetsBalance(assetsInvolved);
    }

    private void logBalanceUpdate(BalanceUpdateEvent balanceEvent) {
        TransactionType transactionType = balanceEvent.getBalanceDelta().compareTo(BigDecimal.ZERO) > 0
                ? TransactionType.DEPOSIT : TransactionType.WITHDRAW;
        final String str = String.format("%s %s %s %s", balanceEvent.getDateTime().format(ISO_DATE_TIME),
                transactionType, balanceEvent.getBalances(), balanceEvent.getBalanceDelta().abs());
        System.out.println(str);
    }

    private void logTrade(OrderTradeUpdateEvent orderEvent, SpotIncomeState prevState) {
        final BigDecimal quoteAssetQty = orderEvent.getOriginalQuantity()
                .multiply(orderEvent.getPriceOfLastFilledTrade());
        String str = String.format("%s %s %s %s for total of %s quoteAsset",
                orderEvent.getDateTime().format(ISO_DATE_TIME), orderEvent.getSide(),
                orderEvent.getOriginalQuantity().toPlainString(), orderEvent.getSymbol(),
                quoteAssetQty.toPlainString());

        if (orderEvent.getSide().equals("SELL")) {
            final String baseAsset = EXCHANGE_INFO.getSymbolInfo(orderEvent.getSymbol()).getBaseAsset();
            final Optional<SpotIncomeState.LockedAsset> lockedState = prevState.findLockedAsset(baseAsset);
            if (lockedState.isPresent()) {
                str += ". Profit:" + quoteAssetQty.subtract(lockedState.get().getStableValue()).toPlainString();
            }
        }
        System.out.println(str);
    }
}