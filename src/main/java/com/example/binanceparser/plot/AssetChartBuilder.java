package com.example.binanceparser.plot;

import static com.example.binanceparser.Constants.*;
import com.example.binanceparser.config.ChartBuilderConfig;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AssetChartBuilder implements ChartBuilder<EventBalanceState> {
	private List<String> assetsToTrack;
	private List<WithdrawPoint> withdrawPoints;
	private ChartBuilderConfig config;

	public AssetChartBuilder(List<String> assetsToTrack) {
		withdrawPoints = new ArrayList<>();
		this.assetsToTrack = assetsToTrack;
		config = ChartBuilderConfig.getDefaultConfig();
	}

	public AssetChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config) {
		withdrawPoints = new ArrayList<>();
		this.assetsToTrack = assetsToTrack;
		this.config = config;
	}

	@Override
	public JFreeChart buildLineChart(List<EventBalanceState> eventBalanceStates) {
		final TimeSeriesCollection dataSeries = new TimeSeriesCollection();
		getTimeSeriesForEveryAsset(eventBalanceStates).forEach(dataSeries::addSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Account balance", "Date", "Balance", dataSeries);
		if (config.isDrawPoints()) {
			chart.getXYPlot().setRenderer(getRenderer());
		}
		return chart;
	}

	private List<TimeSeries> getTimeSeriesForEveryAsset(List<EventBalanceState> incomeStates) {
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		if (incomeStates.size() != 0) {
			assetsToTrack = updateAssetsToTrack(incomeStates.get(incomeStates.size() - 1));
			for (int n = 0; n < assetsToTrack.size(); n++) {
				timeSeriesList.add(createTimeSeries(incomeStates, assetsToTrack.get(n), n));
			}
		}
		return timeSeriesList;
	}

	private TimeSeries createTimeSeries(List<EventBalanceState> eventStates, String trackedAsset, int row) {
		final TimeSeries series = new TimeSeries(trackedAsset + " balance (USD)");
		for (int n = 0; n < eventStates.size(); n++) {
			EventBalanceState eventBalanceState = eventStates.get(n);
			Asset asset = eventBalanceState.findAsset(trackedAsset);
			if (eventBalanceState.getTransactions().stream().anyMatch((
					transaction) -> isStableCoin(transaction.getBaseAsset())
							&& trackedAsset.equals(transaction.getBaseAsset())
							&& transaction.getTransactionType() != null
							&& (transaction.getTransactionType().equals(TransactionType.WITHDRAW)
									|| transaction.getTransactionType().equals(TransactionType.DEPOSIT)))) {
				withdrawPoints.add(new WithdrawPoint(row, n));
				withdrawPoints.add(new WithdrawPoint(0, n));
			}
			series.addOrUpdate(dateTimeToSecond(eventBalanceState.getDateTime()), asset.getBalance());
		}
		return series;
	}

	private Second dateTimeToSecond(LocalDateTime dateTime) {
		return new Second(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
	}

	private List<String> updateAssetsToTrack(EventBalanceState lastIncomeState) {
		return assetsToTrack.isEmpty()
				? new ArrayList<String>(
						lastIncomeState.getAssets().stream().map(Asset::getAsset).collect(Collectors.toList()))
				: assetsToTrack;
	}

	private XYItemRenderer getRenderer() {
		Renderer renderer = new Renderer();
		for (int i = 0; i < assetsToTrack.size(); i++) {
			renderer.setSeriesShape(i, new Rectangle(config.getPointSize(), config.getPointSize()));
		}
		return renderer;
	}

	private boolean isWithdraw(int row, int item) {
		return withdrawPoints.stream()
				.anyMatch((withdrawPoint) -> withdrawPoint.row == row && withdrawPoint.item == item);
	}

	private boolean isStableCoin(String asset) {
		return STABLECOIN_RATE.keySet().stream().anyMatch((stableCoin) -> stableCoin.equals(asset));
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