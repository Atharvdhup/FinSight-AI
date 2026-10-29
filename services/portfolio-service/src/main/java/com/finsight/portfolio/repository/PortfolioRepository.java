package com.finsight.portfolio.repository;

import com.finsight.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserId(String userId);

    Optional<Portfolio> findByIdAndUserId(Long id, String userId);

    boolean existsByIdAndUserId(Long id, String userId);
}

