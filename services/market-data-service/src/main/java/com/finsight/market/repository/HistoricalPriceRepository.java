package com.finsight.market.repository;

import com.finsight.market.entity.HistoricalPrice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistoricalPriceRepository extends JpaRepository<HistoricalPrice, Long> {

    @Query("SELECT hp FROM HistoricalPrice hp WHERE UPPER(hp.stock.symbol) = UPPER(:symbol) AND hp.priceDate BETWEEN :startDate AND :endDate ORDER BY hp.priceDate ASC")
    List<HistoricalPrice> findCandlesBetween(
            @Param("symbol") String symbol,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT hp FROM HistoricalPrice hp WHERE UPPER(hp.stock.symbol) = UPPER(:symbol) ORDER BY hp.priceDate DESC")
    List<HistoricalPrice> findLatestCandles(
            @Param("symbol") String symbol,
            Pageable pageable
    );
}

