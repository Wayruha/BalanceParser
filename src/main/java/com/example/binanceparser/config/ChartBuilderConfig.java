package com.example.binanceparser.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.awt.Color;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartBuilderConfig {
	private boolean drawPoints;
	private boolean drawCross;
	private float crossLength;
	private float crossWidth;
	private Color withdrawColor;

	public static ChartBuilderConfig getDefaultConfig() {
		return new ChartBuilderConfig(true, true, 6, 2, Color.yellow);
	}
}
