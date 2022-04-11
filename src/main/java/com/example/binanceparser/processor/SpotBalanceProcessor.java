package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.sources.EventSource;
import com.example.binanceparser.domain.Asset;
import com.example.binanceparser.domain.balance.SpotBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.transaction.Transaction;
import com.example.binanceparser.domain.transaction.TransactionType;
import com.example.binanceparser.plot.SpotAssetChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.generator.SpotBalanceReportGenerator;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.VIRTUAL_USD;
import static java.lang.String.format;

public class SpotBalanceProcessor extends Processor<BalanceVisualizerConfig, AbstractEvent> {
    private static final Logger log = Logger.getLogger(SpotBalanceProcessor.class.getName());
    private final SpotBalanceReportGenerator balanceReportGenerator;
    private final SpotBalanceCalcAlgorithm algorithm;

    public SpotBalanceProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) throws FileNotFoundException {
        super(config, eventSource);
        final SpotAssetChartBuilder chartBuilder = new SpotAssetChartBuilder(config.getAssetsToTrack());
        this.balanceReportGenerator = new SpotBalanceReportGenerator(config, chartBuilder);
        this.algorithm = new SpotBalanceCalcAlgorithm();
    }

    @Override
    public BalanceReport process(List<AbstractEvent> events) {
        if (events.size() == 0) {
            throw new RuntimeException("Can't find any relevant events");
        }

        final List<SpotBalanceState> balanceStates = algorithm.processEvents(events).stream()
                .filter(state -> inRange(state.getDateTime(), config.getStartTrackDate(), config.getFinishTrackDate()))
                .collect(Collectors.toList());

        BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);
        log.fine("More detailed log:");
        balanceStates.forEach(this::logTransaction);
        log.info("SpotProcessor done for config: " + config);
        return balanceReport;
    }

    private boolean inRange(LocalDateTime date, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return date.compareTo(rangeStart) >= 0 && date.compareTo(rangeEnd) <= 0;
    }

    private void logTransaction(SpotBalanceState state) {
        final Transaction _tx = state.getTXs().get(0);
        final BigDecimal usdBalance = state.findAsset(VIRTUAL_USD).map(Asset::getBalance).orElse(BigDecimal.ZERO);
        if (_tx.getType() == TransactionType.DEPOSIT || _tx.getType() == TransactionType.WITHDRAW) {
            final Transaction.Update tx = (Transaction.Update) _tx;
            final Transaction.Asset2 asset = tx.getAsset();
            final String str = format("%s: %s %s %s. Profit=%s, balance=%s. USD=%s", formatDateTime(tx.getDate()), tx.getType(), formatNumber(asset.getTxQty()),
                    asset.getAssetName(), formatNumber(tx.getValueIncome()), formatNumber(asset.getFullBalance()),
                    formatNumber(usdBalance.setScale(1, RoundingMode.HALF_EVEN)));
            log.fine(str);
        } else {
            final Transaction.Trade tx = (Transaction.Trade) _tx;
            final Transaction.Asset2 base = tx.getBaseAsset();
            final Transaction.Asset2 quote = tx.getQuoteAsset();
            final String str = format("%s: %s %s %s for %s %s. Profit=%s, baseBalance=%s, quoteBalance=%s. USD=%s", formatDateTime(tx.getDate()),
                    tx.getType(), formatNumber(base.getTxQty()), base.getAssetName(), formatNumber(quote.getTxQty()), quote.getAssetName(),
                    formatNumber(tx.getValueIncome()), formatNumber(base.getFullBalance()),
                    formatNumber(quote.getFullBalance()), formatNumber(usdBalance.setScale(1, RoundingMode.HALF_EVEN)));
            log.fine(str);
        }
    }

    private double formatNumber(BigDecimal number) {
        return number.stripTrailingZeros().doubleValue();
    }

    private String formatDateTime(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', ' ');
    }
}