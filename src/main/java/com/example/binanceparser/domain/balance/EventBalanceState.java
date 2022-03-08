package com.example.binanceparser.domain.balance;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.domain.AccountUpdateReasonType;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.events.FuturesAccountUpdateEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.example.binanceparser.domain.transaction.Transaction;
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
    private List<Transaction> TXs;

    public EventBalanceState(LocalDateTime dateTime) {
        super(dateTime);
        this.assets = new LinkedHashSet<>();
        this.TXs = new ArrayList<>();
    }

    public EventBalanceState(LocalDateTime dateTime, EventBalanceState balanceState) {
        super(dateTime);
        this.assets = new LinkedHashSet<>(balanceState.getAssets());
        this.TXs = new ArrayList<>();
    }

    public void updateAssets(List<Asset> newAssets) {
        newAssets.forEach(updatedAsset -> {
            assets.removeIf(currentAsset -> currentAsset.getAsset().equals(updatedAsset.getAsset()));
            assets.add(updatedAsset);
        });
    }

    public void processAccUpdate(FuturesAccountUpdateEvent accEvent) {
        final AccountUpdateReasonType reason = accEvent.getReasonType();
        accEvent.getBalances()
                .forEach(bal -> {
                    Transaction.Asset2 txAsset = Transaction.Asset2.builder()
                            .assetName(bal.getAsset())
                            .txQty(valueOf(bal.getBalanceChange()))
                            .fullBalance(valueOf(bal.getWalletBalance()))
                            .valuableBalance(valueOf(bal.getWalletBalance()))
                            .stableValue(valueOf(bal.getBalanceChange()))
                            .build();
                    switch (reason) {
                        case DEPOSIT:
                            TXs.add(Transaction.depositTx(accEvent.getDateTime(), txAsset, valueOf(bal.getBalanceChange())));
                            break;
                        case WITHDRAW:
                            TXs.add(Transaction.withdrawTx(accEvent.getDateTime(), txAsset, valueOf(bal.getBalanceChange())));
                            break;
                    }
               });
    }

    // Futures does not fully support transactions yet
    public void processTradeEvent(FuturesOrderTradeUpdateEvent event) {
        if (event.getOrderStatus() != OrderStatus.FILLED) return;
        Transaction.Asset2 txAsset = Transaction.Asset2.builder()
                .assetName(event.getSymbol())
                .txQty(event.getOriginalQuantity())
                .fullBalance(null).valuableBalance(null).stableValue(null)
                .build();

        if (event.getSide() == OrderSide.BUY) {
            TXs.add(Transaction.buyTx(event.getDateTime(), txAsset, null, null, null));
        } else {
            TXs.add(Transaction.sellTx(event.getDateTime(), txAsset, null, null, null));
        }
    }

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
        if (assetName.equals(VIRTUAL_USD)) {
            return calculateVirtualUSDBalance();
        }

        final Asset asset = findAsset(assetName);
        return asset == null ? BigDecimal.ZERO : asset.getBalance();
    }
}