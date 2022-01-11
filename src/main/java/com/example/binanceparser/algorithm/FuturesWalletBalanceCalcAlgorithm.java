package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent.Position;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static com.example.binanceparser.Constants.*;

/*
 * this algorithm just uses AccUpdate.walletBalance, without any calculations
 */
public class FuturesWalletBalanceCalcAlgorithm implements CalculationAlgorithm<EventBalanceState> {
	private final BalanceVisualizerConfig config;
	private final Map<String, BigDecimal> currencyRate;

	public FuturesWalletBalanceCalcAlgorithm(BalanceVisualizerConfig config, Map<String, BigDecimal> currencyRate) {
		this.config = config;
		this.currencyRate = currencyRate;
	}

	@Override
	public List<EventBalanceState> processEvents(List<AbstractEvent> allEvents, List<String> assetsToTrack) {
		List<FuturesAccountUpdateEvent> events = allEvents.stream().filter(e -> e instanceof FuturesAccountUpdateEvent)
				.map(e -> (FuturesAccountUpdateEvent) e)
				.collect(Collectors.toList());

		List<EventBalanceState> states = new ArrayList<>();

		events.forEach(e -> {
			logBalanceUpdate(e);
			final List<Asset> assets = e.getBalances().stream()
					.map(asset -> new Asset(asset.getAsset(), BigDecimal.valueOf(asset.getWalletBalance())))
					.collect(Collectors.toList());

			EventBalanceState state = states.size() == 0 ? new EventBalanceState(e.getDateTime(), null)
					: new EventBalanceState(e.getDateTime(), states.get(states.size() - 1), null);

			state.updateAssets(assets);
			e.getBalances().forEach(bal ->
				state.processOrderDetails(e.getReasonType(), bal.getAsset(), "", new BigDecimal(bal.getBalanceChange()), null));
			states.add(state);
		});

		if (config.isConvertToUSD()) {
			calculateUSDCosts(states);
		}
		return states;
	}
	
	private void logBalanceUpdate(FuturesAccountUpdateEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append(event.getDateTime().format(ISO_DATE_TIME)).append(" ")
		.append(event.getReasonType()).append(" balances:{");
		for(FuturesAccountUpdateEvent.Asset asset : event.getBalances()) {
			sb.append("asset: ").append(asset.getAsset())
			.append(", wallet balance:").append(asset.getWalletBalance())
			.append(", balance change:").append(asset.getBalanceChange());
		}
		sb.append("} positions:{");
		for(Position pos : event.getPositions()) {
			sb.append("symbol: ").append(pos.getSymbol())
			.append(", position amount:").append(pos.getPositionAmount())
			.append(", entry price:").append(pos.getEntryPrice());
		}
		sb.append("}");
		System.out.println(sb.toString());
	}

	/**
	 * convert all assets to combined $-value Adds a separate asset - "USD"
	 */
	private void calculateUSDCosts(List<EventBalanceState> assetList) {
		for (EventBalanceState state : assetList) {
			final Optional<BigDecimal> optBalance = totalBalance(state.getAssets());
			optBalance.ifPresent(bal -> state.getAssets().add(new Asset(USD, bal)));
		}
	}

	public static Optional<BigDecimal> totalBalance(Set<Asset> assets) {
		return assets.stream().map(FuturesWalletBalanceCalcAlgorithm::assetToUSD).filter(Objects::nonNull).reduce(BigDecimal::add);
	}

	public static BigDecimal assetToUSD(Asset asset) {
		if (!STABLECOIN_RATE.containsKey(asset.getAsset()))
			return null;
		return asset.getBalance().multiply(STABLECOIN_RATE.get(asset.getAsset()));
	}

	@Override
	public List<EventBalanceState> processEvents(List<AbstractEvent> events) {
		return processEvents(events, config.getAssetsToTrack());
	}
}