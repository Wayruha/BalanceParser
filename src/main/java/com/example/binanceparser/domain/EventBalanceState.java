package com.example.binanceparser.domain;

import com.binance.api.client.domain.OrderSide;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.Constants.STABLECOIN_RATE;
import static com.example.binanceparser.Constants.VIRTUAL_USD;
import static java.math.BigDecimal.valueOf;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class EventBalanceState extends BalanceState {
    private Set<Asset> assets;
    private List<Transaction> transactions;
    private List<TransactionX> TXs;

    public EventBalanceState(LocalDateTime dateTime) {
        super(dateTime);
        // TODO add virtual usd
        this.assets = new LinkedHashSet<>();
        this.transactions = new ArrayList<>();
        this.TXs = new ArrayList<>();
    }

    public EventBalanceState(LocalDateTime dateTime, EventBalanceState balanceState) {
        super(dateTime);
        this.assets = new LinkedHashSet<>(balanceState.getAssets());
        this.transactions = new ArrayList<>();
        this.TXs = new ArrayList<>();
    }

    public void updateAssets(List<Asset> newAssets) {
        newAssets.stream().forEach((updatedAsset) -> {
            assets.removeIf((currentAsset) -> currentAsset.getAsset().equals(updatedAsset.getAsset()));
            assets.add(updatedAsset);
        });
    }

    public void processAccUpdate(FuturesAccountUpdateEvent accEvent) {
        final AccountUpdateReasonType reason = accEvent.getReasonType();
        accEvent.getBalances()
                .forEach(bal -> {
                    TransactionX.Asset2 txAsset = TransactionX.Asset2.builder()
                            .assetName(bal.getAsset())
                            .txQty(valueOf(bal.getBalanceChange()))
                            .fullBalance(valueOf(bal.getWalletBalance()))
                            .valuableBalance(valueOf(bal.getWalletBalance()))
                            .stableValue(valueOf(bal.getBalanceChange()))
                            .build();
                    switch (reason) {
                        case DEPOSIT:
                            TXs.add(TransactionX.depositTx(accEvent.getDateTime(), txAsset, valueOf(bal.getBalanceChange())));
                            break;
                        case WITHDRAW:
                            TXs.add(TransactionX.withdrawTx(accEvent.getDateTime(), txAsset, valueOf(bal.getBalanceChange())));
                            break;
                    }

                    //TODO old part - remove it.
                    if (reason.equals(AccountUpdateReasonType.WITHDRAW)) {
                        transactions.add(new Transaction(TransactionType.WITHDRAW, bal.getAsset(), "", valueOf(bal.getBalanceChange()), null, null));
                    } else if (reason.equals(AccountUpdateReasonType.DEPOSIT)) {
                        transactions.add(new Transaction(TransactionType.DEPOSIT, bal.getAsset(), "", valueOf(bal.getBalanceChange()), null, null));
                    } else {
                        transactions.add(new Transaction());
                    }
                });
    }

    // Futures does not fully support transactions yet
    public void processTradeEvent(FuturesOrderTradeUpdateEvent event) {
        if (!event.getOrderStatus().equals("FILLED")) return;
        TransactionX.Asset2 txAsset = TransactionX.Asset2.builder()
                .assetName(event.getSymbol())
                .txQty(valueOf(event.getOriginalQuantity()))
                .fullBalance(null).valuableBalance(null).stableValue(null)
                .build();

        if (event.getSide() == OrderSide.BUY) {
            TXs.add(TransactionX.buyTx(event.getDateTime(), txAsset, null, null, null));
        } else {
            TXs.add(TransactionX.sellTx(event.getDateTime(), txAsset, null, null, null));
        }
    }

    // will be re-written entirely
    public BigDecimal calculateVirtualUSDBalance() {
        BigDecimal virtualBalance = BigDecimal.ZERO;
        // works for quoteAsset = USD
        for (Asset asset : assets) {
            if (STABLECOIN_RATE.containsKey(asset.getAsset())) {
                virtualBalance = virtualBalance.add(STABLECOIN_RATE.get(asset.getAsset()).multiply(asset.getBalance()));
            }
        }
        return virtualBalance;
    }

    public Asset findAsset(String assetName) {
        return assets.stream().filter(a -> a.getAsset().equals(assetName)).findFirst().orElse(null);
    }

    public BigDecimal getAssetBalance(String assetName) {
        // this will be removed later
        if (assetName.equals(VIRTUAL_USD)) {
            return calculateVirtualUSDBalance();
        }

        final Asset asset = findAsset(assetName);
        return asset == null ? BigDecimal.ZERO : asset.getBalance();
    }
}