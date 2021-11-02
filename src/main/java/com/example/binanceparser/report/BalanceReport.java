package com.example.binanceparser.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BalanceReport {
    LocalDateTime startTrackDate;
    LocalDateTime finishTrackDate;
    BigDecimal max;
    BigDecimal min;
    String outputPath;
    BigDecimal balanceDifference;

    @Override
    public String toString() {
        return "Result:\nStart track date: " + startTrackDate + "\nFinish track date: " + finishTrackDate + "\nBalance difference: " + balanceDifference +
                "\nMax value: " + max + "\nMin value: " + min + "\nOutput path: " + outputPath;
    }
}
