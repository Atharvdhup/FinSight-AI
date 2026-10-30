package com.finsight.market.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.market.dto.SeedStockItem;
import com.finsight.market.entity.HistoricalPrice;
import com.finsight.market.entity.Stock;
import com.finsight.market.entity.StockQuote;
import com.finsight.market.repository.HistoricalPriceRepository;
import com.finsight.market.repository.StockQuoteRepository;
import com.finsight.market.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class MarketDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MarketDataInitializer.class);

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final int HISTORICAL_TRADING_DAYS = 90;

    private final StockRepository stockRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final HistoricalPriceRepository historicalPriceRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public MarketDataInitializer(StockRepository stockRepository,
                                  StockQuoteRepository stockQuoteRepository,
                                  HistoricalPriceRepository historicalPriceRepository,
                                  ObjectMapper objectMapper,
                                  ResourceLoader resourceLoader) {
        this.stockRepository = stockRepository;
        this.stockQuoteRepository = stockQuoteRepository;
        this.historicalPriceRepository = historicalPriceRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (stockRepository.count() > 0) {
            log.info("Market data is already initialized. Skipping seed loader.");
            return;
        }

        log.info("Starting initial seed loading for market securities and 90-day candle history...");
        try {
            Resource resource = resourceLoader.getResource("classpath:data/seed-stocks.json");
            try (InputStream inputStream = resource.getInputStream()) {
                List<SeedStockItem> seedItems = objectMapper.readValue(inputStream, new TypeReference<List<SeedStockItem>>() {});

                // Seed Random for deterministic generation across re-runs
                Random random = new Random(42L);

                for (SeedStockItem item : seedItems) {
                    Stock stock = new Stock(
                            item.getSymbol(),
                            item.getCompanyName(),
                            item.getSector(),
                            item.getIndustry(),
                            item.getCurrency()
                    );
                    stock = stockRepository.save(stock);

                    // Generate 90 trading days of historical candles
                    List<HistoricalPrice> candles = generateHistoricalCandles(stock, item, random);
                    historicalPriceRepository.saveAll(candles);

                    // Determine quote metrics based on the last historical day
                    HistoricalPrice lastCandle = candles.get(candles.size() - 1);
                    BigDecimal previousClose = lastCandle.getClosePrice();

                    // Current price has a small delta from yesterday's close
                    double intradayChangeFactor = 1.0 + ((random.nextDouble() - 0.48) * 0.03); // -1.4% to +1.6%
                    BigDecimal currentPrice = previousClose.multiply(BigDecimal.valueOf(intradayChangeFactor)).setScale(SCALE, ROUNDING);

                    BigDecimal openPrice = previousClose.multiply(BigDecimal.valueOf(1.0 + ((random.nextDouble() - 0.5) * 0.005))).setScale(SCALE, ROUNDING);
                    BigDecimal highPrice = currentPrice.max(openPrice).multiply(BigDecimal.valueOf(1.0 + (random.nextDouble() * 0.01))).setScale(SCALE, ROUNDING);
                    BigDecimal lowPrice = currentPrice.min(openPrice).multiply(BigDecimal.valueOf(1.0 - (random.nextDouble() * 0.01))).setScale(SCALE, ROUNDING);
                    Long volume = Math.round(item.getAverageVolume() * (0.7 + (random.nextDouble() * 0.6)));

                    StockQuote quote = new StockQuote(
                            stock,
                            currentPrice,
                            openPrice,
                            highPrice,
                            lowPrice,
                            previousClose,
                            volume,
                            Instant.now()
                    );
                    stockQuoteRepository.save(quote);
                    log.info("Seeded stock: {} ({}) with initial quote: {} {}", stock.getSymbol(), stock.getCompanyName(), currentPrice, stock.getCurrency());
                }
                log.info("Successfully seeded {} securities and 90-day historical prices.", seedItems.size());
            }
        } catch (Exception e) {
            log.error("Failed to seed initial market data: {}", e.getMessage(), e);
            throw new RuntimeException("Market data seeding failed", e);
        }
    }

    private List<HistoricalPrice> generateHistoricalCandles(Stock stock, SeedStockItem item, Random random) {
        List<HistoricalPrice> candles = new ArrayList<>();
        List<LocalDate> tradingDays = new ArrayList<>();

        // Collect 90 past trading days (Monday to Friday)
        LocalDate dateCursor = LocalDate.now().minusDays(1);
        while (tradingDays.size() < HISTORICAL_TRADING_DAYS) {
            if (dateCursor.getDayOfWeek() != DayOfWeek.SATURDAY && dateCursor.getDayOfWeek() != DayOfWeek.SUNDAY) {
                tradingDays.add(dateCursor);
            }
            dateCursor = dateCursor.minusDays(1);
        }

        // Reverse so dates are chronological (oldest to newest)
        Collections.reverse(tradingDays);

        // Calculate a starting baseline price 90 days ago (~85% to 95% of today's baseline)
        double priceCursor = item.getBaselinePrice().doubleValue() * (0.88 + (random.nextDouble() * 0.06));

        for (LocalDate date : tradingDays) {
            // Daily drift: slight positive drift + daily random volatility
            double dailyReturn = (random.nextDouble() - 0.485) * 0.025; // -1.2% to +1.3%
            double open = priceCursor * (1.0 + ((random.nextDouble() - 0.5) * 0.004));
            double close = open * (1.0 + dailyReturn);
            double high = Math.max(open, close) * (1.0 + (random.nextDouble() * 0.012));
            double low = Math.min(open, close) * (1.0 - (random.nextDouble() * 0.012));
            long volume = Math.round(item.getAverageVolume() * (0.6 + (random.nextDouble() * 0.8)));

            candles.add(new HistoricalPrice(
                    stock,
                    date,
                    BigDecimal.valueOf(open).setScale(SCALE, ROUNDING),
                    BigDecimal.valueOf(high).setScale(SCALE, ROUNDING),
                    BigDecimal.valueOf(low).setScale(SCALE, ROUNDING),
                    BigDecimal.valueOf(close).setScale(SCALE, ROUNDING),
                    volume
            ));

            priceCursor = close;
        }

        return candles;
    }
}
