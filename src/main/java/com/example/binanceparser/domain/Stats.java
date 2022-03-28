package com.example.binanceparser.domain;

import java.math.BigDecimal;

public interface Stats {
    public BigDecimal getOriginalPrice();

    public BigDecimal getClonedPrice(int index);

    public BigDecimal getAverageClonedPrice();

    public Long getDelay(int index);

    public Long getAverageDelay();

    public BigDecimal getOriginalIncome();

    public BigDecimal getClonedIncome(int index);

    public BigDecimal getAverageClonedIncome();
}
