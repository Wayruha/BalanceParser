package com.example.binanceparser.datasource.sources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import com.example.binanceparser.datasource.models.UserApiData;
import com.opencsv.bean.CsvToBeanBuilder;

public class CSVUserDataSource {
	private File csvDir;
	
	public CSVUserDataSource(String filepath) {
		csvDir = new File(filepath);
	}
	
	public List<UserApiData> getUsersData() throws IllegalStateException, FileNotFoundException {
		final List<UserApiData> csvPojo = new CsvToBeanBuilder<UserApiData>(new FileReader(csvDir)).withType(UserApiData.class)
				.withSkipLines(1).build().parse();
		return csvPojo;
	}
}