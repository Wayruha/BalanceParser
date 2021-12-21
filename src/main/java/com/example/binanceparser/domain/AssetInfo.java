package com.example.binanceparser.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetInfo {
	private LocalDateTime dateOfLastTransaction;
	private String quoteAsset;
	private BigDecimal priceOfLastTrade;
}
