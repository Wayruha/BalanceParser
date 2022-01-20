package com.example.binanceparser;

import java.math.BigDecimal;

public class Utils {

    public static String format(BigDecimal num) {
        return num != null ? num.toPlainString() : "-";
    }
}
