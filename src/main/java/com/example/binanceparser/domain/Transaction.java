package com.example.binanceparser.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
	private TransactionType transactionType;
	private String baseAsset;
	private String quoteAsset;
	private BigDecimal assetDelta;
	private BigDecimal price;
	private BigDecimal income;
}
