package com.example.hack1base.events.domain;

import com.example.hack1base.events.dto.SummaryRequest;
import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import com.example.hack1base.salesaggregation.domain.SalesAggregationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportEventListenerTest {

    @Mock
    private SalesAggregationService aggregationService;

    @Mock
    private GithubModelsClients githubModelsClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReportEventListener listener;

    private ReportRequestedEvent mockEvent(LocalDate from, LocalDate to, String branch, String emailTo) {
        SummaryRequest req = mock(SummaryRequest.class);
        when(req.getFrom()).thenReturn(from);
        when(req.getTo()).thenReturn(to);
        when(req.getBranch()).thenReturn(branch);
        when(req.getEmailTo()).thenReturn(emailTo);

        ReportRequestedEvent event = mock(ReportRequestedEvent.class);
        when(event.getRequest()).thenReturn(req);
        return event;
    }

    @Test
    @DisplayName("shouldSendEmailWithSummaryWhenEventIsValid")
    void shouldSendEmailWithSummaryWhenEventIsValid() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 7);
        String branch = "Miraflores";
        String emailTo = "dest@corp.com";

        ReportRequestedEvent event = mockEvent(from, to, branch, emailTo);

        SalesAggregates aggregates = new SalesAggregates(40, 1234.50, "SKU-1", "Miraflores");
        when(aggregationService.calculateAggregates(from, to, branch)).thenReturn(aggregates);
        when(githubModelsClient.generateSummary(aggregates)).thenReturn("Resumen breve en español.");


        assertDoesNotThrow(() -> listener.handleReportRequest(event));


        verify(aggregationService).calculateAggregates(from, to, branch);
        verify(githubModelsClient).generateSummary(aggregates);

        ArgumentCaptor<String> toCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> fromCap = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCapDate = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<SalesAggregates> aggCap = ArgumentCaptor.forClass(SalesAggregates.class);
        ArgumentCaptor<String> summaryCap = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendSummaryEmail(
                toCap.capture(), fromCap.capture(), toCapDate.capture(), aggCap.capture(), summaryCap.capture()
        );

        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals(emailTo, toCap.getValue()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(from, fromCap.getValue()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(to, toCapDate.getValue()),
                () -> org.junit.jupiter.api.Assertions.assertSame(aggregates, aggCap.getValue()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("Resumen breve en español.", summaryCap.getValue())
        );
    }

    @Test
    @DisplayName("shouldSwallowErrorAndStopPipelineWhenAggregationFails")
    void shouldSwallowErrorAndStopPipelineWhenAggregationFails() {

        LocalDate from = LocalDate.of(2025, 2, 1);
        LocalDate to = LocalDate.of(2025, 2, 7);
        String branch = "Surco";
        String emailTo = "dest@corp.com";
        ReportRequestedEvent event = mockEvent(from, to, branch, emailTo);

        when(aggregationService.calculateAggregates(from, to, branch))
                .thenThrow(new RuntimeException("DB down"));

        assertDoesNotThrow(() -> listener.handleReportRequest(event));

        verifyNoInteractions(githubModelsClient);
        verify(emailService, never()).sendSummaryEmail(anyString(), any(), any(), any(), anyString());
    }

    @Test
    @DisplayName("shouldSwallowErrorAndStopPipelineWhenModelClientFails")
    void shouldSwallowErrorAndStopPipelineWhenModelClientFails() {

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 7);
        ReportRequestedEvent event = mockEvent(from, to, "Lince", "dest@corp.com");

        SalesAggregates aggregates = new SalesAggregates(10, 200.0, "SKU-X", "Lince");
        when(aggregationService.calculateAggregates(from, to, "Lince")).thenReturn(aggregates);
        when(githubModelsClient.generateSummary(aggregates)).thenThrow(new RuntimeException("LLM error"));


        assertDoesNotThrow(() -> listener.handleReportRequest(event));


        verify(emailService, never()).sendSummaryEmail(anyString(), any(), any(), any(), anyString());
    }

    @Test
    @DisplayName("shouldSwallowErrorWhenEmailSendingFails")
    void shouldSwallowErrorWhenEmailSendingFails() {

        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 4, 7);
        ReportRequestedEvent event = mockEvent(from, to, "Mira", "mail@corp.com");

        SalesAggregates aggregates = new SalesAggregates(5, 50.0, "SKU-2", "Mira");
        when(aggregationService.calculateAggregates(from, to, "Mira")).thenReturn(aggregates);
        when(githubModelsClient.generateSummary(aggregates)).thenReturn("OK resumen");
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendSummaryEmail(anyString(), any(), any(), any(), anyString());


        assertDoesNotThrow(() -> listener.handleReportRequest(event));


        verify(emailService, times(1))
                .sendSummaryEmail(eq("mail@corp.com"), eq(from), eq(to), eq(aggregates), eq("OK resumen"));
    }
}

