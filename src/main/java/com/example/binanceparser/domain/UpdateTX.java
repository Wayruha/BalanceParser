package com.example.binanceparser.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import static com.example.binanceparser.domain.TransactionType.DEPOSIT;
import static com.example.binanceparser.domain.TransactionType.WITHDRAW;

@Getter
@Setter
public class UpdateTX extends TransactionX {
    Asset2 asset;

    protected UpdateTX(TransactionType type, Asset2 asset, BigDecimal income) {
        super(type, income);
        this.asset = asset;
    }

    public static UpdateTX depositTx(Asset2 asset, BigDecimal income) {
        return new UpdateTX(DEPOSIT, asset, income);
    }

    public static UpdateTX withdrawTx(Asset2 asset, BigDecimal income) {
        return new UpdateTX(WITHDRAW, asset, income);
    }
}
