package com.example.binanceparser.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class FuturesAccountUpdateEvent extends AbstractEvent{

    // Event reason type
    private AccountUpdateReasonType reasonType;

    private List<Asset> balances;

    private List<Position> positions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Asset{

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
