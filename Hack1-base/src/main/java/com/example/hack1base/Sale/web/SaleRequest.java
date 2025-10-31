package com.example.hack1base.Sale.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SaleRequest {
    @NotBlank(message = "SKU es obligatorio")
    private String sku;

    @Min(value = 1, message = "Units debe ser mayor que 0")
    private int units;

    @Min(value = 1, message = "Price debe ser mayor que 0")
    private double price;

    @NotBlank(message = "Branch es obligatorio")
    private String branch;

    @NotNull(message = "soldAt es obligatorio")
    private LocalDateTime soldAt;
}
