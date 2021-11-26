package com.example.binanceparser.domain;

public enum IncomeType {
    TRANSFER("TRANSFER"),// deposit or withdraw
    WELCOME_BONUS("WELCOME_BONUS"),
    REALIZED_PNL("REALIZED_PNL"),// filled position
    FUNDING_FEE("FUNDING_FEE"),
    COMMISSION("COMMISION"),
    INSURANCE_CLEAR("INSURANCE_CLEAR");

    private final String incomeTypeId;

    IncomeType(String incomeTypeId) {
        this.incomeTypeId = incomeTypeId;
    }
}
