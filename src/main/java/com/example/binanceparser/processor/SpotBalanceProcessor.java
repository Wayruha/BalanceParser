package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.TransactionType;
import com.example.binanceparser.domain.TransactionX;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.plot.TestAssetChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.TestBalanceReportGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.VIRTUAL_USD;
import static java.lang.String.format;

public class SpotBalanceProcessor extends Processor<BalanceVisualizerConfig> {
	private final EventSource<AbstractEvent> eventSource;
    private final TestBalanceReportGenerator balanceReportGenerator;
    private final SpotBalanceCalcAlgorithm algorithm;

    public SpotBalanceProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) {
        super(config);
        this.eventSource = eventSource;
        final TestAssetChartBuilder chartBuilder = new TestAssetChartBuilder(config.getAssetsToTrack());
        this.balanceReportGenerator = new TestBalanceReportGenerator(config, chartBuilder);
        this.algorithm = new SpotBalanceCalcAlgorithm();
    }

    @Override
    public BalanceReport process() throws IOException {
        final List<AbstractEvent> events = eventSource.getData();
        if (events.size() == 0)
            throw new RuntimeException("Can't find any relevant events");
        // retrieve balance changes
        List<SpotIncomeState> balanceStates = algorithm.processEvents(events).stream()
                .filter(state -> inRange(state.getDateTime(), config.getStartTrackDate(), config.getFinishTrackDate()))
                .collect(Collectors.toList());

        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);
        final List<AbstractEvent> periodRelevantEvents = events.stream().filter(e -> inRange(e.getDateTime(), config.getStartTrackDate(), config.getFinishTrackDate()))
                .collect(Collectors.toList());
//        System.out.println("Transferred to Futures total: " + FuturesBalanceStateProcessor.calculateDepositDelta(periodRelevantEvents));
        System.out.println("More detailed log:");
        balanceStates.forEach(this::logTransaction);
        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }
    
    private boolean inRange(LocalDateTime date, LocalDateTime rangeStart, LocalDateTime rangeEnd){
        return date.compareTo(rangeStart) >= 0 && date.compareTo(rangeEnd) <= 0;
    }

    private void logTransaction(SpotIncomeState state){
        final TransactionX _tx = state.getTXs().get(0);
        final BigDecimal usdBalance = state.findAssetOpt(VIRTUAL_USD).map(Asset::getBalance).orElse(BigDecimal.ZERO);
        if(_tx.getType() == TransactionType.DEPOSIT || _tx.getType() == TransactionType.WITHDRAW){
            final TransactionX.Update tx = (TransactionX.Update) _tx;
            final TransactionX.Asset2 asset = tx.getAsset();
            final String str = format("%s: %s %s %s. Profit=%s, balance=%s. USD=%s", formatDateTime(tx.getDate()), tx.getType(), formatNumber(asset.getTxQty()),
                    asset.getAssetName(), formatNumber(tx.getValueIncome()), formatNumber(asset.getFullBalance()),
                    formatNumber(usdBalance.setScale(1, RoundingMode.HALF_EVEN)));
            System.out.println(str);
        } else {
            final TransactionX.Trade tx = (TransactionX.Trade) _tx;
            final TransactionX.Asset2 base = tx.getBaseAsset();
            final TransactionX.Asset2 quote = tx.getQuoteAsset();
            final String str = format("%s: %s %s %s for %s %s. Profit=%s, baseBalance=%s, quoteBalance=%s. USD=%s", formatDateTime(tx.getDate()),
                    tx.getType(), formatNumber(base.getTxQty()), base.getAssetName(), formatNumber(quote.getTxQty()), quote.getAssetName(),
                    formatNumber(tx.getValueIncome()), formatNumber(base.getFullBalance()),
                    formatNumber(quote.getFullBalance()), formatNumber(usdBalance.setScale(1, RoundingMode.HALF_EVEN)));
            System.out.println(str);
        }
    }

    private double formatNumber(BigDecimal number) {
        return number.stripTrailingZeros().doubleValue();
    }

    private String formatDateTime(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', ' ');
    }
}