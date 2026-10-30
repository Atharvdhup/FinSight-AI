package com.finsight.market.service;

import com.finsight.common.dto.StockQuoteDTO;
import com.finsight.market.dto.HistoricalCandleDTO;
import com.finsight.market.dto.StockDetailResponse;
import com.finsight.market.entity.HistoricalPrice;
import com.finsight.market.entity.Stock;
import com.finsight.market.entity.StockQuote;
import com.finsight.market.exception.ResourceNotFoundException;
import com.finsight.market.repository.HistoricalPriceRepository;
import com.finsight.market.repository.StockQuoteRepository;
import com.finsight.market.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final StockRepository stockRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final HistoricalPriceRepository historicalPriceRepository;

    public MarketDataService(StockRepository stockRepository,
                             StockQuoteRepository stockQuoteRepository,
                             HistoricalPriceRepository historicalPriceRepository) {
        this.stockRepository = stockRepository;
        this.stockQuoteRepository = stockQuoteRepository;
        this.historicalPriceRepository = historicalPriceRepository;
    }

    /**
     * Retrieve all active securities with their latest quote snapshots.
     * Uses JOIN FETCH query to prevent N+1 queries.
     */
    public List<StockDetailResponse> getAllActiveStocks() {
        List<Stock> stocks = stockRepository.findAllActiveWithQuotes();
        return stocks.stream()
                .map(this::mapToDetailResponse)
                .toList();
    }

    /**
     * Retrieve a single stock's full details and latest quote snapshot.
     */
    public StockDetailResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbolWithQuote(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with symbol: " + symbol));

        return mapToDetailResponse(stock);
    }

    /**
     * Retrieve historical daily OHLCV candles for a given ticker symbol.
     */
    public List<HistoricalCandleDTO> getHistoricalCandles(String symbol, Integer days, LocalDate from, LocalDate to) {
        // Validate stock existence first
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with symbol: " + symbol));

        LocalDate startDate;
        LocalDate endDate = (to != null) ? to : LocalDate.now();

        if (from != null) {
            startDate = from;
        } else {
            int rangeDays = (days != null && days > 0) ? days : 30;
            startDate = endDate.minusDays(rangeDays);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }

        List<HistoricalPrice> candles = historicalPriceRepository.findCandlesBetween(stock.getSymbol(), startDate, endDate);

        return candles.stream()
                .map(this::mapToCandleDTO)
                .toList();
    }

    /**
     * Helper to return standard StockQuoteDTO from common-dto module.
     */
    public StockQuoteDTO getStockQuoteDTO(String symbol) {
        Stock stock = stockRepository.findBySymbolWithQuote(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with symbol: " + symbol));

        StockQuote quote = stock.getQuote();
        return StockQuoteDTO.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .currency(stock.getCurrency())
                .currentPrice(quote != null ? quote.getCurrentPrice() : null)
                .previousClose(quote != null ? quote.getPreviousClose() : null)
                .change(quote != null ? quote.getChangeAmount() : null)
                .changePercent(quote != null ? quote.getChangePct() : null)
                .openPrice(quote != null ? quote.getOpenPrice() : null)
                .highPrice(quote != null ? quote.getHighPrice() : null)
                .lowPrice(quote != null ? quote.getLowPrice() : null)
                .volume(quote != null ? quote.getVolume() : null)
                .marketTime(quote != null ? quote.getMarketTime() : null)
                .build();
    }

    private StockDetailResponse mapToDetailResponse(Stock stock) {
        StockQuote quote = stock.getQuote();
        return StockDetailResponse.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .industry(stock.getIndustry())
                .currency(stock.getCurrency())
                .currentPrice(quote != null ? quote.getCurrentPrice() : null)
                .change(quote != null ? quote.getChangeAmount() : null)
                .changePercent(quote != null ? quote.getChangePct() : null)
                .openPrice(quote != null ? quote.getOpenPrice() : null)
                .highPrice(quote != null ? quote.getHighPrice() : null)
                .lowPrice(quote != null ? quote.getLowPrice() : null)
                .previousClose(quote != null ? quote.getPreviousClose() : null)
                .volume(quote != null ? quote.getVolume() : null)
                .marketTime(quote != null ? quote.getMarketTime() : null)
                .build();
    }

    private HistoricalCandleDTO mapToCandleDTO(HistoricalPrice price) {
        return HistoricalCandleDTO.builder()
                .symbol(price.getStock().getSymbol())
                .priceDate(price.getPriceDate())
                .openPrice(price.getOpenPrice())
                .highPrice(price.getHighPrice())
                .lowPrice(price.getLowPrice())
                .closePrice(price.getClosePrice())
                .volume(price.getVolume())
                .build();
    }
}
