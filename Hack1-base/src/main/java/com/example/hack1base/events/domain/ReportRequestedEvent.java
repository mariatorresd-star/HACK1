package com.example.hack1base.events.domain;

import com.example.hack1base.events.dto.SummaryRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReportRequestedEvent extends ApplicationEvent {
    private final SummaryRequest request;
    private final String requestId;
    private final String requestedBy;

    public ReportRequestedEvent(Object source, SummaryRequest request, String requestId, String requestedBy) {
        super(source);
        this.request = request;
        this.requestId = requestId;
        this.requestedBy = requestedBy;
    }
}

