package com.finsight.market.repository;

import com.finsight.market.entity.StockQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockQuoteRepository extends JpaRepository<StockQuote, Long> {

    Optional<StockQuote> findByStockSymbol(String symbol);

    Optional<StockQuote> findByStockSymbolIgnoreCase(String symbol);
}

