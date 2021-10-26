package com.example.binanceparser.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


public enum AccountUpdateReasonType {
    DEPOSIT,
    WITHDRAW,
    ORDER,
    FUNDING_FEE,
    WITHDRAW_REJECT,
    ADJUSTMENT,
    INSURANCE_CLEAR,
    ADMIN_DEPOSIT,
    ADMIN_WITHDRAW,
    MARGIN_TRANSFER,
    MARGIN_TYPE_CHANGE,
    ASSET_TRANSFER,
    OPTIONS_PREMIUM_FEE,
    OPTIONS_SETTLE_PROFIT,
    COIN_SWAP_DEPOSIT,
    COIN_SWAP_WITHDRAW
    ;
}
