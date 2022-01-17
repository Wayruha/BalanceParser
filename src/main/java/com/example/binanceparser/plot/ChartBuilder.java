package com.example.binanceparser.plot;

import com.example.binanceparser.config.ChartBuilderConfig;
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

import static com.example.binanceparser.Constants.STABLECOIN_RATE;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class ChartBuilder<T extends BalanceState> {
    public abstract JFreeChart buildLineChart(List<T> logBalanceStates);
    
	protected List<String> assetsToTrack;
	protected List<WithdrawPoint> withdrawPoints;
	protected ChartBuilderConfig config;
    
	public ChartBuilder(){
		
	}
	
	public ChartBuilder(List<String> assetsToTrack) {
		this.withdrawPoints = new ArrayList<>();
		this.assetsToTrack = assetsToTrack;
		this.config = ChartBuilderConfig.getDefaultConfig();
	}
	
	public ChartBuilder(List<String> assetsToTrack, ChartBuilderConfig config){
		this.withdrawPoints = new ArrayList<>();
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
		return isStableCoin(transaction.getBaseAsset())
				&& transaction.getBaseAsset().equals(trackedAsset) // TODO ця стрічка скоріш за все не має багато сенсу
				&& (transaction.getTransactionType().equals(TransactionType.WITHDRAW)
						|| transaction.getTransactionType().equals(TransactionType.DEPOSIT));
	}

	//TODO повністю переробити - я зробив цей метод як копію існуючого тільки щоб не поломати код
	protected boolean isTransfer(String trackedAsset, TransactionX transaction) {
		if(transaction.getType() == TransactionType.WITHDRAW || transaction.getType() == TransactionType.DEPOSIT){
			final TransactionX.Update tx = (TransactionX.Update) transaction;
			final TransactionX.Asset2 asset = tx.getAsset();
			return isStableCoin(asset.getAssetName()) && asset.getAssetName().equals(trackedAsset);
		}
		return false;
	}
	
    @Data
	@NoArgsConstructor
	@AllArgsConstructor
	class WithdrawPoint {
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
			return super.getItemPaint(row, item);
		}

		@Override
		public Shape getItemShape(int row, int item) {
			if (isWithdraw(row, item) && config.isDrawCross()) {
				return ShapeUtils.createDiagonalCross(config.getCrossLength(), config.getCrossWidth());
			}
			return super.getItemShape(row, item);
		}

		private boolean isWithdraw(int row, int item) {
			return withdrawPoints.stream()
					.anyMatch((withdrawPoint) -> withdrawPoint.row == row && withdrawPoint.item == item);
		}
	}
}