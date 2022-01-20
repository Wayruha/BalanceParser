package com.example.binanceparser.plot;

import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.TransactionType;
import com.example.binanceparser.domain.TransactionX;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.Second;

import static com.example.binanceparser.Constants.*;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ChartBuilder<T extends BalanceState> {
	public abstract JFreeChart buildLineChart(List<T> logBalanceStates);

	protected List<String> assetsToTrack;
	protected List<Point> withdrawPoints;
	protected List<Point> specialPoints;
	protected List<Point> intermediatePoints;
	protected ChartBuilderConfig config;

	public ChartBuilder() {

	}

	public ChartBuilder(List<String> assetsToTrack) {
		this.withdrawPoints = new ArrayList<>();
		this.specialPoints = new ArrayList<>();
		this.intermediatePoints = new ArrayList<>();
		this.assetsToTrack = assetsToTrack;
		this.config = ChartBuilderConfig.getDefaultConfig();
	}

	public ChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		this.withdrawPoints = new ArrayList<>();
		this.specialPoints = new ArrayList<>();
		this.intermediatePoints = new ArrayList<>();
		this.assetsToTrack = assetsToTrack;
		this.config = config;
	}

	protected boolean isStableCoin(String asset) {
		return STABLECOIN_RATE.keySet().stream().anyMatch((stableCoin) -> stableCoin.equals(asset));
	}

	protected Second dateTimeToSecond(LocalDateTime dateTime) {
		return new Second(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
	}

	protected XYItemRenderer getRenderer() {
		Renderer renderer = new Renderer();
		for (int i = 0; i < assetsToTrack.size(); i++) {
			renderer.setSeriesShape(i, new Rectangle(config.getPointSize(), config.getPointSize()));
		}
		return renderer;
	}

	protected boolean isTransfer(String trackedAsset, com.example.binanceparser.domain.Transaction transaction) {
		return isStableCoin(transaction.getBaseAsset()) && transaction.getBaseAsset().equals(trackedAsset) // TODO ця
																											// стрічка
																											// скоріш за
																											// все не
																											// має
																											// багато
																											// сенсу
				&& (transaction.getTransactionType().equals(TransactionType.WITHDRAW)
						|| transaction.getTransactionType().equals(TransactionType.DEPOSIT));
	}

	// TODO повністю переробити - я зробив цей метод як копію існуючого тільки щоб
	// не поломати код
	protected boolean isTransfer(String trackedAsset, TransactionX transaction) {
		if (transaction.getType() == TransactionType.WITHDRAW || transaction.getType() == TransactionType.DEPOSIT) {
			final TransactionX.Update tx = (TransactionX.Update) transaction;
			final TransactionX.Asset2 asset = tx.getAsset();
			return isStableCoin(asset.getAssetName()) && asset.getAssetName().equals(trackedAsset)
					&& transaction.getValueIncome().compareTo(BigDecimal.ZERO) != 0;
		}
		return false;
	}

	protected boolean anyTransfer(List<TransactionX> transactions) {
		return transactions.stream()
				.filter((transaction) -> transaction.getType().equals(TransactionType.DEPOSIT)
						|| transaction.getType().equals(TransactionType.WITHDRAW))
				.anyMatch((transaction) -> {
					final TransactionX.Update tx = (TransactionX.Update) transaction;
					final TransactionX.Asset2 asset = tx.getAsset();
					return isTransfer(asset.getAssetName(), transaction);
				});
	}

	private List<TransactionX> getAllTransactionsToProcess(List<TransactionX> transactions) {
		return transactions.stream().filter((transaction) -> {
			final TransactionX.Trade tx = (TransactionX.Trade) transaction;
			final TransactionX.Asset2 asset = tx.getQuoteAsset();

			return transaction.getType().equals(TransactionType.SELL)
					&& transaction.getValueIncome().compareTo(asset.getTxQty()) < 0;
		}).collect(Collectors.toList());
	}

	private List<TransactionX> getTransactionsToProcess(String trackedAsset, List<TransactionX> transactions) {
		if (VIRTUAL_USD.equals(trackedAsset)) {
			return getAllTransactionsToProcess(transactions);
		} else {
			return getAllTransactionsToProcess(transactions).stream()
					.filter((transaction) -> ((TransactionX.Trade) transaction).getBaseAsset().getAssetName()
							.equals(trackedAsset))
					.collect(Collectors.toList());
		}
	}

	protected long secondsBetween(Second start, Second finish) {
		return ChronoUnit.SECONDS.between(milsToLocaldateLime(start.getLastMillisecond()),
				milsToLocaldateLime(finish.getLastMillisecond()));
	}

	protected Asset getAssetToProcess(String trackedAsset, List<TransactionX> transactions) {
		// overall unlocked amount
		BigDecimal val = BigDecimal.ZERO;
		for (TransactionX transaction : getTransactionsToProcess(trackedAsset, transactions)) {
			final TransactionX.Trade tx = (TransactionX.Trade) transaction;
			final TransactionX.Asset2 asset = tx.getQuoteAsset();
			val = val.add(asset.getTxQty()).subtract(transaction.getValueIncome());
		}
		return new Asset(VIRTUAL_USD, val);
	}

	private LocalDateTime milsToLocaldateLime(long val) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(val), ZoneId.systemDefault());
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	class Point {
		private int row;
		private int item;
	}

	class Renderer extends XYLineAndShapeRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Paint getItemPaint(int row, int item) {
			if (isWithdraw(row, item)) {
				return config.getWithdrawColor();
			}
			if (isSpecialPoint(row, item)) {
				return config.getSpetialPointColor();
			}
			return super.getItemPaint(row, item);
		}

		@Override
		public Shape getItemShape(int row, int item) {
			if ((isWithdraw(row, item) || isSpecialPoint(row, item)) && config.isDrawCross()) {
				return ShapeUtils.createDiagonalCross(config.getCrossLength(), config.getCrossWidth());
			}
			if (isIntermadiatePoint(row, item)) {
				return ShapeUtils.createDiamond(0);// no point
			}
			return super.getItemShape(row, item);
		}

		private boolean isWithdraw(int row, int item) {
			return withdrawPoints.stream().anyMatch((point) -> point.row == row && point.item == item);
		}

		private boolean isSpecialPoint(int row, int item) {
			return specialPoints.stream().anyMatch((point) -> point.row == row && point.item == item);
		}

		private boolean isIntermadiatePoint(int row, int item) {
			return intermediatePoints.stream().anyMatch((point) -> point.row == row && point.item == item);
		}
	}
}