package com.example.hack1base.salesaggregation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesAggregates {
    private int totalUnits;
    private double totalRevenue;
    private String topSku;
    private String topBranch;
}
