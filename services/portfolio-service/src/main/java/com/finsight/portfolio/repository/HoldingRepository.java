package com.finsight.portfolio.repository;

import com.finsight.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByPortfolioId(Long portfolioId);

    Optional<Holding> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);

    List<Holding> findBySymbol(String symbol);

    void deleteByPortfolioIdAndSymbol(Long portfolioId, String symbol);

    @Query("SELECT h FROM Holding h WHERE h.portfolio.id = :portfolioId AND h.quantity > 0 ORDER BY h.currentValue DESC")
    List<Holding> findActiveHoldingsByPortfolioId(@Param("portfolioId") Long portfolioId);
}

