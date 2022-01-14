package com.example.binanceparser.domain;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
class Asset2 {
    String assetName;
    BigDecimal txQty;
    BigDecimal fullBalance;
    BigDecimal valuableBalance;
    BigDecimal stableValue;
}
