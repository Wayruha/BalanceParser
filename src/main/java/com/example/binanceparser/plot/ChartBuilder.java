package com.example.binanceparser.plot;

import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.balance.BalanceState;
import com.example.binanceparser.domain.transaction.Transaction;
import com.example.binanceparser.domain.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.*;

public abstract class ChartBuilder<T extends BalanceState> {
    public abstract JFreeChart buildLineChart(List<T> logBalanceStates);

    protected TimeSeriesCollection dataSeries;
    protected List<String> assetsToTrack;
    protected List<Point> withdrawPoints;
    protected List<Point> specialPoints;
    protected List<Point> intermediatePoints;
    protected ChartBuilderConfig config;

    public ChartBuilder() {
        dataSeries = new TimeSeriesCollection();
    }

    public ChartBuilder(List<String> assetsToTrack) {
        this.withdrawPoints = new ArrayList<>();
        this.specialPoints = new ArrayList<>();
        this.intermediatePoints = new ArrayList<>();
        this.assetsToTrack = assetsToTrack;
        this.config = ChartBuilderConfig.getDefaultConfig();
        dataSeries = new TimeSeriesCollection();
    }

    public ChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
        this.withdrawPoints = new ArrayList<>();
        this.specialPoints = new ArrayList<>();
        this.intermediatePoints = new ArrayList<>();
        this.assetsToTrack = assetsToTrack;
        this.config = config;
        dataSeries = new TimeSeriesCollection();
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

    // метод имеет ту же функцию, что и прошлая версия, проверяет является ли
    // транзакция выводом или депозитом стейблкоина
    protected boolean isTransfer(String trackedAsset, Transaction transaction) {
        if (transaction.getType() == TransactionType.WITHDRAW || transaction.getType() == TransactionType.DEPOSIT) {
            final Transaction.Update tx = (Transaction.Update) transaction;
            final Transaction.Asset2 asset = tx.getAsset();
            return isStableCoin(asset.getAssetName()) && asset.getAssetName().equals(trackedAsset)
                    && transaction.getValueIncome().compareTo(BigDecimal.ZERO) != 0;
        }
        return false;
    }

    protected boolean anyTransfer(List<Transaction> transactions) {
        return transactions.stream().filter(transaction -> transaction.getType().equals(TransactionType.DEPOSIT)
                || transaction.getType().equals(TransactionType.WITHDRAW)).anyMatch(transaction -> {
            final Transaction.Update tx = (Transaction.Update) transaction;
            final Transaction.Asset2 asset = tx.getAsset();
            return isTransfer(asset.getAssetName(), transaction);
        });
    }

    private List<Transaction> getAllTransactionsToProcess(List<Transaction> transactions) {
        return transactions.stream().filter((transaction) -> {
            if (!transaction.getType().equals(TransactionType.SELL)) {
                return false;
            }
            final Transaction.Trade tx = (Transaction.Trade) transaction;
            final Transaction.Asset2 asset = tx.getBaseAsset();

            return tx.getValuableBaseQtyInvolved().compareTo(asset.getTxQty()) < 0;
        }).collect(Collectors.toList());
    }

    private List<Transaction> getTransactionsToProcess(String trackedAsset, List<Transaction> transactions) {
        if (VIRTUAL_USD.equals(trackedAsset)) {
            return getAllTransactionsToProcess(transactions);
        } else {
            return getAllTransactionsToProcess(transactions).stream()
                    .filter((transaction) -> ((Transaction.Trade) transaction).getBaseAsset().getAssetName()
                            .equals(trackedAsset))
                    .collect(Collectors.toList());
        }
    }

    protected long secondsBetween(Second start, Second finish) {
        return ChronoUnit.SECONDS.between(milsToLocalDateTime(start.getLastMillisecond()),
                milsToLocalDateTime(finish.getLastMillisecond()));
    }

    protected Map<String, BigDecimal> getNonValuableAssetTradeParts(String trackedAsset,
                                                                    List<Transaction> transactions) {
        Map<String, BigDecimal> assetsToProcess = new HashMap<>();
        for (Transaction transaction : getTransactionsToProcess(trackedAsset, transactions)) {
            final Transaction.Trade tx = (Transaction.Trade) transaction;
            final Transaction.Asset2 quoteAsset = tx.getQuoteAsset();
            final Transaction.Asset2 baseAsset = tx.getBaseAsset();
            BigDecimal legalQuoteAssetQty = tx.getValuableBaseQtyInvolved().divide(baseAsset.getTxQty(), MATH_CONTEXT)
                    .multiply(quoteAsset.getTxQty());
            String assetName = trackedAsset.equals(VIRTUAL_USD) ? VIRTUAL_USD : quoteAsset.getAssetName();
            if (!assetsToProcess.containsKey(assetName)) {
                assetsToProcess.put(assetName, BigDecimal.ZERO);
            }
            BigDecimal nonValuableBalance = assetsToProcess.get(assetName).add(quoteAsset.getTxQty())
                    .subtract(legalQuoteAssetQty);
            assetsToProcess.put(assetName, nonValuableBalance.divide(quoteAsset.getTxQty(), MATH_CONTEXT));
        }
        return assetsToProcess;
    }

    protected void updateSpecialAndWithdrawPoints(int row, int insertedItem) {
        withdrawPoints.stream().filter((point) -> point.getRow() == row && point.item >= insertedItem)
                .forEach((point) -> {
                    point.setItem(point.getItem() + 1);
                });
        specialPoints.stream().filter((point) -> point.getRow() == row && point.item >= insertedItem)
                .forEach((point) -> {
                    point.setItem(point.getItem() + 1);
                });
    }

    private LocalDateTime milsToLocalDateTime(long val) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(val), ZoneId.systemDefault());
    }


    public LegendItemSource formLegend(LegendItem... items) {
        return () -> {
            LegendItemCollection legendItems = new LegendItemCollection();
            for (LegendItem item : items) {
                legendItems.add(item);
            }
            return legendItems;
        };
    }

    protected LegendItem coinTransferLegendItem() {
        return new LegendItem("Coin deposit/withdraw",
                "first part of a line shows how much of asset sold was valuable", null, null,
                ShapeUtils.createDiagonalCross(config.getCrossLength(), config.getCrossWidth()),
                config.getSpecialPointColor());
    }

    protected LegendItem usdTransferLegendItem() {
        return new LegendItem("USD deposit/withdraw", null, null, null,
                ShapeUtils.createDiagonalCross(config.getCrossLength(), config.getCrossWidth()),
                config.getWithdrawColor());
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
            if (isSpecialPoint(row, item)) {
                return config.getSpecialPointColor();
            }
            if (isWithdraw(row, item)) {
                return config.getWithdrawColor();
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