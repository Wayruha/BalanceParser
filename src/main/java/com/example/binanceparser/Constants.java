package com.example.binanceparser;

import com.binance.api.client.domain.general.ExchangeInfo;
import com.example.binanceparser.binance.BinanceClient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String BINANCE_API_KEY = "KKroawNIP67Om6zT4kP8AEBQRKSDaGHFNQrn9oi7FQ20e4oZHLLvFhGyBajRJEu7";
    public static final String BINANCE_SECRET_KEY = "VIn9YBUN107QLJznKeFXmNnxY6kgmY0i7ol4JbwIXmCi6wDCD24z5kBGjHe3poQf";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String CLONE_POSTFIX = "_cln";
    //paths
    public static final String FUTURES_PROPS_PATH = "src/main/resources/futures-balance.properties";
    public static final String SPOT_PROPS_PATH = "src/main/resources/spot-balance.properties";
    public static final String STATS_PROPS_PATH = "src/main/resources/stats-visualisation.properties";
    public static final String FUTURES_INCOME_PROPS_PATH = "src/main/resources/futures-income.properties";
    public static final String TRADES_COMPARATOR_PROPS_PATH = "src/main/resources/trades-comparator.properties";
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

    public static final ExchangeInfo EXCHANGE_INFO = new BinanceClient(BINANCE_API_KEY, BINANCE_SECRET_KEY).loadExchangeInfo();
    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;
    public static final Map<String, BigDecimal> STABLECOIN_RATE = new HashMap<>();

    static {
        STABLECOIN_RATE.put(USDT, BigDecimal.ONE);
        STABLECOIN_RATE.put(BUSD, BigDecimal.ONE);
    }

    public static boolean isStableCoin(String asset) {
        return STABLECOIN_RATE.containsKey(asset);
    }
}
