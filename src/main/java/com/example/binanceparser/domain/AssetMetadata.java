package com.example.binanceparser.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Value
@Builder
public class AssetMetadata {
	private LocalDateTime dateOfLastTransaction;
	//  quote asset of last transaction
	private String quoteAsset;
	private BigDecimal priceOfLastTrade;
}
