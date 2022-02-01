package com.example.binanceparser.datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import com.opencsv.bean.CsvToBeanBuilder;

public class CSVIncomeSource {
	private File csvDir;
	
	public CSVIncomeSource(String filepath) {
		csvDir = new File(filepath);
	}
	
	public List<CSVIncomeModel> getIncomeModels() throws IllegalStateException, FileNotFoundException {
		final List<CSVIncomeModel> csvPojo = new CsvToBeanBuilder<CSVIncomeModel>(new FileReader(csvDir)).withType(CSVIncomeModel.class)
				.withSkipLines(1).build().parse();
		return csvPojo;
	}
}