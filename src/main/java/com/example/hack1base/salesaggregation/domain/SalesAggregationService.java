package com.example.hack1base.salesaggregation.domain;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.estructure.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SalesAggregationService {

	private final SaleRepository saleRepository;

	public SalesAggregationService(SaleRepository saleRepository) {
		this.saleRepository = saleRepository;
	}

	public SalesAggregates calculateAggregates(LocalDate from, LocalDate to, String branch) {
		if (from == null && to == null) {
			LocalDate today = LocalDate.now();
			to = today;
			from = today.minusDays(6);
		}
		if (from == null || to == null) {
			throw new IllegalArgumentException("Both from and to must be provided when one is set");
		}
		if (to.isBefore(from)) {
			throw new IllegalArgumentException("Invalid date range: to < from");
		}

		LocalDateTime start = from.atStartOfDay();
		LocalDateTime end = to.atTime(LocalTime.MAX);

		List<Sale> sales = (branch == null || branch.isBlank())
				? saleRepository.findBySoldAtBetween(start, end)
				: saleRepository.findByBranchAndSoldAtBetween(branch, start, end);

		if (sales.isEmpty()) {
			return new SalesAggregates(0, 0.0, null, null);
		}

		int totalUnits = sales.stream().mapToInt(Sale::getUnits).sum();
		double totalRevenue = sales.stream().mapToDouble(s -> s.getUnits() * s.getPrice()).sum();

		Map<String, Integer> unitsBySku = sales.stream()
				.collect(Collectors.toMap(Sale::getSku, Sale::getUnits, Integer::sum));
		String topSku = unitsBySku.entrySet().stream()
				.sorted(Comparator
						.comparing(Map.Entry<String, Integer>::getValue).reversed()
						.thenComparing(Map.Entry::getKey))
				.map(Map.Entry::getKey)
				.findFirst().orElse(null);

		Map<String, Integer> unitsByBranch = sales.stream()
				.collect(Collectors.toMap(Sale::getBranch, Sale::getUnits, Integer::sum));
		String topBranch = unitsByBranch.entrySet().stream()
				.sorted(Comparator
						.comparing(Map.Entry<String, Integer>::getValue).reversed()
						.thenComparing(Map.Entry::getKey))
				.map(Map.Entry::getKey)
				.findFirst().orElse(null);

		return new SalesAggregates(totalUnits, totalRevenue, topSku, topBranch);
	}
}
