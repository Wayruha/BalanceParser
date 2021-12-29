package com.example.binanceparser.plot;

import java.awt.Shape;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Paint;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.Transaction;
import com.example.binanceparser.domain.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TestAssetChartBuilder implements ChartBuilder<SpotIncomeState> {
	private List<String> assetsToTrack;
	private List<WithdrawPoint> withdrawPoints;
	private ChartBuilderConfig config;

	public TestAssetChartBuilder(List<String> assetsToTrack) {
		this.assetsToTrack = assetsToTrack;
		withdrawPoints = new ArrayList<>();
		config = ChartBuilderConfig.getDefaultConfig();
	}

	public TestAssetChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		this.config = config;
	}

	@Override
	public JFreeChart buildLineChart(List<SpotIncomeState> incomeStates) {
		TimeSeriesCollection dataSeries = new TimeSeriesCollection();
		getTimeSeriesForEveryAsset(incomeStates).forEach(dataSeries::addSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account income", "Date", "Income", dataSeries);
		if (config.isDrawPoints()) {
			chart.getXYPlot().setRenderer(getRenderer());
		}
		return chart;
	}

	private XYItemRenderer getRenderer() {
		return new Renderer();
	}

	private List<TimeSeries> getTimeSeriesForEveryAsset(List<SpotIncomeState> incomeStates) {
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		if (incomeStates.size() != 0) {
			assetsToTrack = updateAssetsToTrack(incomeStates.get(incomeStates.size() - 1));
			for (int n = 0; n < assetsToTrack.size(); n++) {
				timeSeriesList.add(createTimeSeries(incomeStates, assetsToTrack.get(n), n));
			}
		}
		return timeSeriesList;
	}

	private TimeSeries createTimeSeries(List<SpotIncomeState> incomeStates, String trackedAsset, int row) {
		final TimeSeries series = new TimeSeries(trackedAsset + " balance (USD)");
		BigDecimal assetIncome = BigDecimal.ZERO;
		for (int n = 0, item = 0; n < incomeStates.size(); n++) {
			SpotIncomeState incomeState = incomeStates.get(n);
			for (Transaction transaction : incomeState.getTransactions()) {
				if (transaction.getBaseAsset().equals(trackedAsset)) {
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

	private class Renderer extends XYLineAndShapeRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Paint getItemPaint(int row, int item) {
			if (isWithdraw(row, item)) {
				return config.getWithdrawColor();
			}
			return super.getItemPaint(row, item);
		}

		@Override
		public Shape getItemShape(int row, int item) {
			if (isWithdraw(row, item) && config.isDrawCross()) {
				return ShapeUtils.createDiagonalCross(config.getCrossLength(), config.getCrossWidth());
			}
			return super.getItemShape(row, item);
		}
	}
}