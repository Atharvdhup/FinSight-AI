package com.finsight.portfolio.repository;

import com.finsight.portfolio.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findByPortfolioIdOrderByExecutedAtDesc(Long portfolioId, Pageable pageable);

    List<Transaction> findByPortfolioIdAndSymbolOrderByExecutedAtDesc(Long portfolioId, String symbol);
}

