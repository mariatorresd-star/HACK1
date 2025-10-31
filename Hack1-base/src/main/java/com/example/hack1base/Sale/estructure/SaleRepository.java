package com.example.hack1base.Sale.estructure;

import com.example.hack1base.Sale.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
	List<Sale> findBySoldAtBetween(LocalDateTime from, LocalDateTime to);
	List<Sale> findByBranchAndSoldAtBetween(String branch, LocalDateTime from, LocalDateTime to);
}