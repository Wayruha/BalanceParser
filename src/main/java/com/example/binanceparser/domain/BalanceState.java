package com.example.binanceparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class BalanceState {
    private LocalDateTime dateTime;
    
    public LocalDate getDate(){
        return dateTime != null ? dateTime.toLocalDate() : null;
    }
    
    //TODO delete after refactor
    public BigDecimal getBalanceState() {
    	throw new UnsupportedOperationException("should not use this method");
    }
}
