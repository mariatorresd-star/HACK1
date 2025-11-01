package com.example.hack1base.events.domain;


import com.example.hack1base.events.dto.SummaryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportRequestedEventTest {

    @Test
    @DisplayName("shouldCreateEventWithCorrectFieldsWhenConstructed")
    void shouldCreateEventWithCorrectFieldsWhenConstructed() {

        Object source = new Object();
        SummaryRequest summaryRequest = mock(SummaryRequest.class);
        when(summaryRequest.getBranch()).thenReturn("Miraflores");
        when(summaryRequest.getEmailTo()).thenReturn("admin@oreo.com");

        String requestId = "REQ-2025-01";
        String requestedBy = "user123";


        ReportRequestedEvent event = new ReportRequestedEvent(source, summaryRequest, requestId, requestedBy);


        assertAll(
                () -> assertEquals(source, event.getSource(), "El source debería ser el mismo objeto"),
                () -> assertEquals(summaryRequest, event.getRequest(), "El request debería coincidir"),
                () -> assertEquals(requestId, event.getRequestId(), "El requestId debería coincidir"),
                () -> assertEquals(requestedBy, event.getRequestedBy(), "El requestedBy debería coincidir")
        );
    }

    @Test
    @DisplayName("shouldReturnSameRequestValuesWhenAccessed")
    void shouldReturnSameRequestValuesWhenAccessed() {

        SummaryRequest summaryRequest = mock(SummaryRequest.class);
        when(summaryRequest.getBranch()).thenReturn("Lima Centro");
        when(summaryRequest.getEmailTo()).thenReturn("branch@oreo.com");


        ReportRequestedEvent event = new ReportRequestedEvent(this, summaryRequest, "REQ-999", "supervisor");


        assertEquals("REQ-999", event.getRequestId());
        assertEquals("supervisor", event.getRequestedBy());
        assertEquals("Lima Centro", event.getRequest().getBranch());
        assertEquals("branch@oreo.com", event.getRequest().getEmailTo());
    }
}

