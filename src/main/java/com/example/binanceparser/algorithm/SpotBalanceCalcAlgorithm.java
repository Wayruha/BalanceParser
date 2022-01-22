package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.*;
import com.example.binanceparser.domain.events.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class SpotBalanceCalcAlgorithm implements CalculationAlgorithm<SpotIncomeState> {
	private final int MAX_SECONDS_DELAY_FOR_VALID_EVENTS = 1;

	public SpotBalanceCalcAlgorithm() {
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
				BalanceUpdateEvent balanceEvent = (BalanceUpdateEvent) currentEvent;
				logBalanceUpdate(balanceEvent);
				processBalanceUpdate(incomeState, balanceEvent, (AccountPositionUpdateEvent) nextEvent);
				spotIncomeStates.add(incomeState);
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

	public void processBalanceUpdate(SpotIncomeState state, BalanceUpdateEvent balanceEvent,
			AccountPositionUpdateEvent accEvent) {
		final BigDecimal balanceDelta = balanceEvent.getBalanceDelta();

		if (balanceDelta.signum() <= 0) {
			handleWithdraw(state, balanceEvent, accEvent);
		} else {
			handleDeposit(state, balanceEvent, accEvent);
		}
		state.findAssetOpt(VIRTUAL_USD).get().setBalance(state.calculateVirtualUSDBalance());
	}

	public void processOrder(SpotIncomeState state, OrderTradeUpdateEvent orderEvent,
			AccountPositionUpdateEvent accEvent) {
		if (orderEvent.getSide().equals("BUY") && isStableCoin(orderEvent.getQuoteAsset())) {
			handleBuy(state, orderEvent, accEvent);
		} else if (orderEvent.getSide().equals("SELL") && isStableCoin(orderEvent.getQuoteAsset())) {
			handleSell(state, orderEvent, accEvent);
		} else {
			handleConvertOperation(state, orderEvent, accEvent);
		}
		state.findAssetOpt(VIRTUAL_USD).get().setBalance(state.calculateVirtualUSDBalance());
	}

	//TODO залежить ще від order.side=BUY | SELL
	/**
	 * Цей метод враховує тільки "легальну" частину ассета за який ми щось купляємо.
	 * Віповідно, ми додаємо в lockAssets тільки ту частину купленої монети
	 * (baseAsset), яку можемо оплатити "легальними" коштами. Решта монети не
	 * враховується
	 **/
	public void handleConvertOperation(SpotIncomeState state, OrderTradeUpdateEvent orderEvent,
			AccountPositionUpdateEvent accEvent) {
		final String baseAssetName = orderEvent.getBaseAsset();
		final String quoteAssetName = orderEvent.getQuoteAsset();
		final BigDecimal orderQty = orderEvent.getActualQty();
		final BigDecimal quoteOrderQty = orderEvent.getQuoteAssetQty();
		final Optional<LockedAsset> optLockedQuoteAsset = state.findLockedAsset(quoteAssetName);
		if (optLockedQuoteAsset.isPresent()) {
			final LockedAsset lockedQuoteAsset = optLockedQuoteAsset.get();
			// deduct from locked quote asset
			final BigDecimal cappedQuoteAssetQty = quoteOrderQty.min(lockedQuoteAsset.getBalance());
			BigDecimal stableValueUsed = lockedQuoteAsset.getStableValue();
			lockedQuoteAsset.deductBalance(cappedQuoteAssetQty);
			stableValueUsed = stableValueUsed.subtract(lockedQuoteAsset.getStableValue());

			// create state for base asset
			final BigDecimal valuableQuoteAssetFraction = lockedQuoteAsset.getBalance()
					.divide(quoteOrderQty, MathContext.DECIMAL64).max(ONE);
			final BigDecimal lockedBaseQty = valuableQuoteAssetFraction.multiply(orderQty, MATH_CONTEXT);
			final LockedAsset lockedBaseAsset = state.addLockedAssetIfNotExist(baseAssetName);
			lockedBaseAsset.addBalance(lockedBaseQty, stableValueUsed);
			state.addLockedAsset(lockedBaseAsset);
		}

		updateAssetsBalance(state, orderEvent, accEvent);

		final Optional<Asset> baseAsset = state.findAssetOpt(quoteAssetName);
		final Optional<Asset> quoteAsset = state.findAssetOpt(quoteAssetName);
		final Optional<LockedAsset> lockedBase = state.findLockedAsset(baseAssetName);
		final Optional<LockedAsset> lockedQuote = state.findLockedAsset(quoteAssetName);
		TransactionX.Asset2 base = TransactionX.Asset2.builder().assetName(baseAssetName)
				.txQty(orderEvent.getActualQty()).fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO))
				.valuableBalance(lockedBase.map(LockedAsset::getBalance).orElse(ZERO))
				.stableValue(lockedBase.map(LockedAsset::getStableValue).orElse(ZERO)).build();
		TransactionX.Asset2 quote = TransactionX.Asset2.builder().assetName(quoteAssetName).txQty(quoteOrderQty)
				.fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
				.valuableBalance(lockedQuote.map(LockedAsset::getBalance).orElse(ZERO))
				.stableValue(lockedQuote.map(LockedAsset::getStableValue).orElse(ZERO)).build();
		state.getTXs().add(TransactionX.convertTx(base, quote));
	}

	// TODO ми платимо комісію за операцію, мабуть потрібно тут її вказувати в income
	/**
	 * просто збільшуємо баланс/stableValue LockedAsset'у якщо він вже існує.
	 */
	public void handleBuy(SpotIncomeState state, OrderTradeUpdateEvent orderEvent,
			AccountPositionUpdateEvent accEvent) {
		final String baseAssetName = orderEvent.getBaseAsset();
		final String quoteAssetName = orderEvent.getQuoteAsset();
		final BigDecimal baseQty = orderEvent.getActualQty();
		final BigDecimal quoteQty = orderEvent.getQuoteAssetQty();

		updateAssetsBalance(state, orderEvent, accEvent);

		final LockedAsset baseLocked = state.addLockedAssetIfNotExist(baseAssetName);
		baseLocked.addBalance(baseQty, quoteQty);

		final Optional<Asset> baseAsset = state.findAssetOpt(baseAssetName);
		final Optional<Asset> quoteAsset = state.findAssetOpt(quoteAssetName);
		final Optional<LockedAsset> quoteLocked = state.findLockedAsset(quoteAssetName);
		TransactionX.Asset2 base = TransactionX.Asset2.builder().assetName(baseAssetName).txQty(baseQty)
				.fullBalance(baseAsset.map(Asset::getBalance).orElse(ZERO)).valuableBalance(baseLocked.getBalance())
				.stableValue(baseLocked.getStableValue()).build();
		TransactionX.Asset2 quote = TransactionX.Asset2.builder().assetName(quoteAssetName).txQty(quoteQty)
				.fullBalance(quoteAsset.map(Asset::getBalance).orElse(ZERO))
				.valuableBalance(quoteLocked.map(LockedAsset::getBalance).orElse(ZERO))
				.stableValue(quoteLocked.map(LockedAsset::getStableValue).orElse(ZERO)).build();

		state.getTXs().add(TransactionX.buyTx(base, quote, orderEvent.getQuoteAssetCommission().negate()));
	}

	/**
	 * if baseQty > lockedQty then we calculate the profit using only part of baseAsset
	 * (since another part comes from unknown source quoteAssetEarnedWithValuableFunds = quoteAssetQty * (lockedQty / baseQty)
	 * lockedQty.balance -= baseQtyCapped (locked part of asset)
	 * lockedQty.stableValue -= {proportionally to balance change in prev. line}
	 * income = {what we got selling 'valuable' asset - stableValue of that asset}
	 */
	private void handleSell(SpotIncomeState state, OrderTradeUpdateEvent orderEvent,
			AccountPositionUpdateEvent accEvent) {
		final String baseAssetName = orderEvent.getBaseAsset();
		final String quoteAssetName = orderEvent.getQuoteAsset();
		final BigDecimal baseQty = orderEvent.getActualQty();
		final BigDecimal quoteAssetQty = orderEvent.getQuoteAssetQty();

		final Optional<LockedAsset> baseAssetLocked = state.findLockedAsset(baseAssetName);

		final BigDecimal lockedQty = baseAssetLocked.map(Asset::getBalance).orElse(ZERO);
		final BigDecimal baseQtyCapped = baseQty.min(lockedQty);

		BigDecimal stableValueUnlocked = ZERO;
		BigDecimal quoteAssetEarnedWithValuableFunds = quoteAssetQty;
		if (baseQty.compareTo(lockedQty) > 0) {
			quoteAssetEarnedWithValuableFunds = quoteAssetQty.multiply(lockedQty.divide(baseQty, MATH_CONTEXT),
					MATH_CONTEXT);
		}

		// if there is no locked (or 'valuable') qty then user does not profit from such operation
		if (baseAssetLocked.isPresent()) {
			final LockedAsset locked = baseAssetLocked.get();
			final BigDecimal stableValueBefore = locked.getStableValue();
			locked.deductBalance(baseQtyCapped);
			stableValueUnlocked = stableValueBefore.subtract(locked.getStableValue());
		}
		final BigDecimal income = quoteAssetEarnedWithValuableFunds.subtract(stableValueUnlocked);

		updateAssetsBalance(state, orderEvent, accEvent);

		final Asset baseAsset = state.findAssetOpt(baseAssetName).get(); // not null because we CAN sell it
		final Optional<Asset> quoteAsset = state.findAssetOpt(quoteAssetName);
		TransactionX.Asset2 base = TransactionX.Asset2.builder().assetName(baseAssetName).txQty(baseQty)
				.fullBalance(baseAsset.getBalance())
				.valuableBalance(baseAssetLocked.map(LockedAsset::getBalance).orElse(ZERO))
				.stableValue(baseAssetLocked.map(LockedAsset::getStableValue).orElse(ZERO)).build();
		final BigDecimal quoteAssetBalance = quoteAsset.map(Asset::getBalance).orElse(ZERO); // quoteAsset is always stablecoin

		TransactionX.Asset2 quote = TransactionX.Asset2.builder().assetName(quoteAssetName).txQty(quoteAssetQty)
				.fullBalance(quoteAssetBalance).valuableBalance(quoteAssetBalance).stableValue(quoteAssetBalance)
				.build();

		state.getTXs().add(TransactionX.sellTx(base, quote, income.subtract(orderEvent.getQuoteAssetCommission())));
	}

	private void handleDeposit(SpotIncomeState state, BalanceUpdateEvent balanceEvent,
			AccountPositionUpdateEvent accEvent) {
		final String assetName = balanceEvent.getBalances();
		final BigDecimal assetQty = balanceEvent.getBalanceDelta();

		updateAssetsBalance(state, balanceEvent, accEvent);

		final Optional<Asset> existingAsset = state.findAssetOpt(assetName);
		final Optional<LockedAsset> lockedAsset = state.findLockedAsset(assetName);
		TransactionX.Asset2 txAsset = TransactionX.Asset2.builder().assetName(assetName).txQty(assetQty)
				.fullBalance(existingAsset.map(Asset::getBalance).orElse(ZERO))
				.valuableBalance(lockedAsset.map(LockedAsset::getBalance).orElse(ZERO))
				.stableValue(lockedAsset.map(LockedAsset::getStableValue).orElse(ZERO)).build();

		final BigDecimal transactionStableValue = STABLECOIN_RATE.getOrDefault(assetName, ZERO).multiply(assetQty);
		state.getTXs().add(TransactionX.depositTx(txAsset, transactionStableValue));
	}

	public void handleWithdraw(SpotIncomeState state, BalanceUpdateEvent balanceEvent,
			AccountPositionUpdateEvent accEvent) {
		final String assetName = balanceEvent.getBalances();
		final BigDecimal qty = balanceEvent.getBalanceDelta().abs();
		final Optional<Asset> assetOpt = state.findAssetOpt(assetName);
		
		/* примечание для себя
		 * сейчас если апдейт происходит в 282 строке, то получается, что если мы
		 * попытаемся вывести монету и ДО этого у нас не было с ней операцийб то ее не
		 * будет в списке карентАссетс, а если обновить в 254 строке, то мы вытянем уже
		 * ОБНОВЛЕННОЕ значение после трейда, так как вытягиваем его из AccPosUpd,
		 * иногда вылeтает NoSuchElement
		 */

		Optional<LockedAsset> optLocked = state.findLockedAsset(assetName);;
		BigDecimal stableValueDiff = ZERO;
		//when we try to withdraw asset we have worked with before (if not, assetOpt is empty)
		if(assetOpt.isPresent()) {
			final Asset existingAsset = state.findAssetOpt(assetName).get();
			final BigDecimal valuableAssetBalance = optLocked.map(Asset::getBalance).orElse(ZERO);
			final BigDecimal nonValuableAssetBalance = existingAsset.getBalance().subtract(valuableAssetBalance);
			// first, we withdraw as much nonValuable asset as possible and only then withdraw with valuable part
			final BigDecimal withdrawValuableQty = qty.subtract(nonValuableAssetBalance).max(ZERO);

			if (optLocked.isPresent()) {
				final LockedAsset locked = optLocked.get();
				final BigDecimal stableValueBefore = locked.getStableValue();
				locked.deductBalance(withdrawValuableQty);
				stableValueDiff = locked.getStableValue().subtract(stableValueBefore);
			}
		}
		
		updateAssetsBalance(state, balanceEvent, accEvent);
		optLocked = state.findLockedAsset(assetName);
		TransactionX.Asset2 txAsset = TransactionX.Asset2.builder().assetName(assetName).txQty(qty)
				.fullBalance(state.findAssetOpt(assetName).map(Asset::getBalance).orElse(ZERO))
				.valuableBalance(optLocked.map(LockedAsset::getBalance).orElse(ZERO))
				.stableValue(optLocked.map(LockedAsset::getStableValue).orElse(ZERO)).build();
		state.getTXs().add(TransactionX.withdrawTx(txAsset, stableValueDiff));
	}

	private void updateAssetsBalance(SpotIncomeState state, BalanceUpdateEvent balanceEvent,
			AccountPositionUpdateEvent accEvent) {
		final String baseAsset = balanceEvent.getBalances();
		final LocalDateTime dateTime = balanceEvent.getDateTime();
		updateAssetsBalance(state, accEvent, baseAsset, dateTime);
	}

	private void updateAssetsBalance(SpotIncomeState state, OrderTradeUpdateEvent orderEvent, AccountPositionUpdateEvent accEvent) {
		final String baseAsset = orderEvent.getBaseAsset();
		final LocalDateTime dateTime = orderEvent.getDateTime();
		updateAssetsBalance(state, accEvent, baseAsset, dateTime);
	}

	private void updateAssetsBalance(SpotIncomeState state, AccountPositionUpdateEvent accEvent, String baseAsset,
			LocalDateTime dateTime) {
		final AssetMetadata assetMetadata = AssetMetadata.builder().dateOfLastTransaction(dateTime).build();
		final List<Asset> assetsInvolved = accEvent.getBalances().stream()
				.map(asset -> Asset.builder().asset(asset.getAsset()).balance(asset.getFree().add(asset.getLocked()))
						.assetMetadata(asset.getAsset().equals(baseAsset) ? assetMetadata : null).build())
				.collect(Collectors.toList());
		state.updateAssetsBalance(assetsInvolved);
	}

	private void logBalanceUpdate(BalanceUpdateEvent balanceEvent) {
		TransactionType transactionType = balanceEvent.getBalanceDelta().compareTo(BigDecimal.ZERO) > 0
				? TransactionType.DEPOSIT
				: TransactionType.WITHDRAW;
		final String str = String.format("%s %s %s %s", balanceEvent.getDateTime().format(ISO_DATE_TIME),
				transactionType, balanceEvent.getBalances(), balanceEvent.getBalanceDelta().abs());
		System.out.println(str);
	}

	private void logTrade(OrderTradeUpdateEvent orderEvent, SpotIncomeState prevState) {
		final BigDecimal quoteAssetQty = orderEvent.getOriginalQuantity()
				.multiply(orderEvent.getPriceOfLastFilledTrade());
		String str = String.format("%s %s %s %s for total of %s quoteAsset",
				orderEvent.getDateTime().format(ISO_DATE_TIME), orderEvent.getSide(),
				orderEvent.getOriginalQuantity().stripTrailingZeros().doubleValue(), orderEvent.getSymbol(),
				quoteAssetQty.setScale(1, RoundingMode.HALF_EVEN).toPlainString());

		if (orderEvent.getSide().equals("SELL")) {
			final String baseAsset = EXCHANGE_INFO.getSymbolInfo(orderEvent.getSymbol()).getBaseAsset();
			final Optional<LockedAsset> lockedState = prevState.findLockedAsset(baseAsset);
			if (lockedState.isPresent()) {
				str += ". Profit:" + quoteAssetQty.subtract(lockedState.get().getStableValue()).toPlainString();
			}
		}
		System.out.println(str);
	}
}