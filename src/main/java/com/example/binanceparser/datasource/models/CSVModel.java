package com.example.binanceparser.datasource.models;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSVModel {
  @CsvBindByPosition(position = 0)
  private String customer_id;
  @CsvBindByPosition(position = 1)
  private String event_type;
  @CsvBindByPosition(position = 2)
  private String event_ts;
  @CsvBindByPosition(position = 3)
  private String json;
}
