package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceState {
    LocalDate dateTime;
    List<Asset> assets;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Asset {
        String asset;
        BigDecimal availableBalance;
    }
}
