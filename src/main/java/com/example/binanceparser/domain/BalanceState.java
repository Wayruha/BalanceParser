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
	
	private BigDecimal balanceState;
    private LocalDateTime dateTime;

    public LocalDate getDate(){
        return dateTime != null ? dateTime.toLocalDate() : null;
    }
    
    public BigDecimal getBalanceState() {
    	return balanceState;
    }
}
