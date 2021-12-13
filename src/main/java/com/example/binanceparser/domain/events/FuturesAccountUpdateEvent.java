package com.example.binanceparser.domain.events;

import com.example.binanceparser.domain.AccountUpdateReasonType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class FuturesAccountUpdateEvent extends AbstractEvent {

    // Event reason type
    private AccountUpdateReasonType reasonType;

    private List<Asset> balances;

    private List<Position> positions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Asset {

        private String asset;

        private Double walletBalance;

        private Double crossWalletBalance;

        private Double balanceChange;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Position {

        private String symbol;

        private String positionAmount;

        private String entryPrice;

        private String accumulatedRealized;

        private String unrealizedPnL;

        private String marginType;

        private String isolatedWallet;
    }
}
