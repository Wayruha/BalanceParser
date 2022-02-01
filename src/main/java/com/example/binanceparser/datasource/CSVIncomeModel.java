package com.example.binanceparser.datasource;

import com.opencsv.bean.CsvBindByPosition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSVIncomeModel {
	@CsvBindByPosition(position = 0)
	private String userId;
	@CsvBindByPosition(position = 1)
	private String apiKey;
	@CsvBindByPosition(position = 2)
	private String secretKey;
}