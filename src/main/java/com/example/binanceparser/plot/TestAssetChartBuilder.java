package com.example.binanceparser.plot;

import static com.example.binanceparser.Constants.*;
import java.awt.Shape;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Paint;
import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.Transaction;
import com.example.binanceparser.domain.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TestAssetChartBuilder implements ChartBuilder<SpotIncomeState> {
	private List<String> assetsToTrack;
	private List<WithdrawPoint> withdrawPoints;

	public TestAssetChartBuilder(List<String> assetsToTrack) {
		this.assetsToTrack = assetsToTrack;
		withdrawPoints = new ArrayList<>();
	}

	@Override
	public JFreeChart buildLineChart(List<SpotIncomeState> incomeStates) {
		TimeSeriesCollection dataSeries = new TimeSeriesCollection();
		dataSeries.addSeries(getOverallIncomeTimeSeries(incomeStates));
		if (!assetsToTrack.equals(List.of(USD))) {
			getTimeSeriesForEveryAsset(incomeStates).stream().forEachOrdered((series) -> dataSeries.addSeries(series));
		}
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account income", "Date", "Income", dataSeries);
		chart.getXYPlot().setRenderer(getRenderer());
		return chart;
	}

	private XYItemRenderer getRenderer() {
		XYItemRenderer renderer = new XYLineAndShapeRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Paint getItemPaint(int row, int item) {
				if (isWithdraw(row, item)) {
					return Color.yellow;
				}
				return super.getItemPaint(row, item);
			}

			@Override
			public Shape getItemShape(int row, int item) {
				if (isWithdraw(row, item)) {
					return ShapeUtils.createDiagonalCross(6, 2);
				}
				return super.getItemShape(row, item);
			}
		};
		return renderer;
	}

	private TimeSeries getOverallIncomeTimeSeries(List<SpotIncomeState> incomeStates) {
		final TimeSeries series = new TimeSeries("Overall income (USD)");
		for (int item = 0; item < incomeStates.size(); item++) {
			SpotIncomeState incomeState = incomeStates.get(item);
			series.addOrUpdate(dateTimeToSecond(incomeState.getDateTime()),
					incomeState.getBalanceState().doubleValue());
			if (incomeState.getTransactions().stream()
					.anyMatch((transaction) -> transaction.getTransactionType().equals(TransactionType.WITHDRAW))) {
				withdrawPoints.add(new WithdrawPoint(0, item));
			}
		}
		return series;
	}

	private List<TimeSeries> getTimeSeriesForEveryAsset(List<SpotIncomeState> incomeStates) {
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		if (incomeStates.size() != 0) {
			assetsToTrack = updateAssetsToTrack(incomeStates.get(incomeStates.size() - 1));
			for (int n = 0; n < assetsToTrack.size(); n++) {
				timeSeriesList.add(createTimeSeries(incomeStates, assetsToTrack.get(n), n + 1));
			}
		}
		return timeSeriesList;
	}

	private TimeSeries createTimeSeries(List<SpotIncomeState> incomeStates, String assetToTrack, int row) {
		final TimeSeries series = new TimeSeries(assetToTrack + " income (USD)");
		BigDecimal assetIncome = BigDecimal.ZERO;
		for (int n = 0, item = 0; n < incomeStates.size(); n++) {
			SpotIncomeState incomeState = incomeStates.get(n);
			for (Transaction transaction : incomeState.getTransactions()) {
				if (transaction.getBaseAsset().equals(assetToTrack)) {
					assetIncome = assetIncome.add(transaction.getIncome());
					if (transaction.getTransactionType().equals(TransactionType.WITHDRAW)) {
						withdrawPoints.add(new WithdrawPoint(row, item));
					}
					if (series.addOrUpdate(dateTimeToSecond(incomeState.getDateTime()), assetIncome) == null) {
						item++;
					}
				}
			}
		}
		return series;
	}

	private boolean isWithdraw(int row, int item) {
		return withdrawPoints.stream()
				.anyMatch((withdrawPoint) -> withdrawPoint.row == row && withdrawPoint.item == item);
	}

	private List<String> updateAssetsToTrack(SpotIncomeState lastIncomeState) {
		return assetsToTrack.isEmpty() ? new ArrayList<String>(lastIncomeState.getCurrentAssets().stream()
				.map((asset) -> asset.getAsset()).collect(Collectors.toList())) : assetsToTrack;
	}

	private Second dateTimeToSecond(LocalDateTime dateTime) {
		return new Second(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private class WithdrawPoint {
		private int row;
		private int item;
	}
}