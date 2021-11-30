package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Income {
    String symbol;
    IncomeType incomeType;
    BigDecimal income;
    String asset;
    Long time;
    String info;
    String tranId;
    String tradeId;
}
