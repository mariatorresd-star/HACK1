package com.example.hack1base.events.domain;

import com.example.hack1base.events.dto.SummaryRequest;
import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import com.example.hack1base.salesaggregation.domain.SalesAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportEventListener {

    private final SalesAggregationService aggregationService;
    private final GithubModelsClient githubModelsClient;
    private final EmailService emailService;

    @Async
    @EventListener
    public void handleReportRequest(ReportRequestedEvent event) {
        SummaryRequest req = event.getRequest();

        try {
            SalesAggregates aggregates = aggregationService.calculateAggregates(req.getFrom(), req.getTo(), req.getBranch());

            String summary = githubModelsClient.generateSummary(aggregates);

            emailService.sendSummaryEmail(req.getEmailTo(), req.getFrom(), req.getTo(), aggregates, summary);

        } catch (Exception e) {
            System.err.println("❌ Error procesando resumen: " + e.getMessage());
        }
    }
}

