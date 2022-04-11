package com.example.binanceparser.datasource.models;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserName {
    @CsvBindByPosition(position = 0)
    private String user;
    @CsvBindByPosition(position = 1)
    private String name;
}
