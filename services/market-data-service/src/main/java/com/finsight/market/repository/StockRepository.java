package com.finsight.market.repository;

import com.finsight.market.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    Optional<Stock> findBySymbolIgnoreCase(String symbol);

    List<Stock> findByIsActiveTrueOrderBySymbolAsc();

    @Query("SELECT s FROM Stock s LEFT JOIN FETCH s.quote WHERE s.isActive = true ORDER BY s.symbol ASC")
    List<Stock> findAllActiveWithQuotes();

    @Query("SELECT s FROM Stock s LEFT JOIN FETCH s.quote WHERE UPPER(s.symbol) = UPPER(:symbol)")
    Optional<Stock> findBySymbolWithQuote(@Param("symbol") String symbol);
}

