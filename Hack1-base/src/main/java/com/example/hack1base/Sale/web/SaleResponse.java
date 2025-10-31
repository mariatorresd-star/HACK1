package com.example.hack1base.Sale.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private String sku;
    private int units;
    private double price;
    private String branch;
    private LocalDateTime soldAt;
    private String createdBy; 
    private LocalDateTime createdAt;
}
