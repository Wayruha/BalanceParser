package com.example.binanceparser.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetMetadata {
	private LocalDateTime dateOfLastTransaction;
	//  quote asset of last transaction
	private String quoteAsset;
	private BigDecimal priceOfLastTrade;
}
