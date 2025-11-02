package com.example.hack1base.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryRequest {
    private LocalDate from;
    private LocalDate to;
    private String branch;
    private String emailTo;
}

