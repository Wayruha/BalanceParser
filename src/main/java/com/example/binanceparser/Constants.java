package com.example.binanceparser;

import com.binance.api.client.domain.general.ExchangeInfo;
import com.example.binanceparser.binance.BinanceClient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    //    public static final String BINANCE_API_KEY = "KKroawNIP67Om6zT4kP8AEBQRKSDaGHFNQrn9oi7FQ20e4oZHLLvFhGyBajRJEu7";
//    public static final String BINANCE_SECRET_KEY = "VIn9YBUN107QLJznKeFXmNnxY6kgmY0i7ol4JbwIXmCi6wDCD24z5kBGjHe3poQf";
    public static final String BINANCE_API_KEY = "NL5wPsMNrUpllAQQgng6zJ0K8ExJaQtJRfQmcE8dYRhEjLu43lGqVqEkfWUdSREa";
    public static final String BINANCE_SECRET_KEY = "HsOmFtFsXQQoiq9EFzz7BURWXhovT4uVTWUGD7grwekvGIsfn2xM7CSbuc70KV5o";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String CLONE_POSTFIX = "_cln";
    //coins
    public static final String USD = "USD";
    public static final String USDT = "USDT";
    public static final String BUSD = "BUSD";
    public static final String ETH = "ETH";
    public static final String BTC = "BTC";
    public static final String AXS = "AXS";
    public static final String BNB = "BNB";
    public static final String VIRTUAL_USD = "VIRTUAL_USD";
    //...

    public static BinanceClient BINANCE_CLIENT = new BinanceClient(BINANCE_API_KEY, BINANCE_SECRET_KEY);
    public static ExchangeInfo EXCHANGE_INFO = BINANCE_CLIENT.loadExchangeInfo();
    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;
    public static final Map<String, BigDecimal> STABLECOIN_RATE = new HashMap<>();

    static {
        STABLECOIN_RATE.put(USDT, BigDecimal.ONE);
        STABLECOIN_RATE.put(BUSD, BigDecimal.ONE);
    }

    public static boolean isStableCoin(String asset) {
        return STABLECOIN_RATE.containsKey(asset);
    }

    public static void updateBinanceClient(String apiKey, String secretkey) {
        BINANCE_CLIENT = new BinanceClient(apiKey, secretkey);
        EXCHANGE_INFO = BINANCE_CLIENT.loadExchangeInfo();
    }
}
