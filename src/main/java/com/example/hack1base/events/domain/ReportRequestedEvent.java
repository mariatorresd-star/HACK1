package com.example.hack1base.events.domain;

import com.example.hack1base.events.dto.SummaryRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReportRequestedEvent extends ApplicationEvent {

    private final SummaryRequest request;
    private final String requestId;
    private final String requestedBy;

    private final boolean premium;
    private final boolean includeCharts;
    private final boolean attachPdf;

    public ReportRequestedEvent(Object source, SummaryRequest request, String requestId, String requestedBy) {
        this(source, request, requestId, requestedBy, false, false, false);
    }

    public ReportRequestedEvent(Object source, SummaryRequest request, String requestId, String requestedBy,
                                boolean premium, boolean includeCharts, boolean attachPdf) {
        super(source);
        this.request = request;
        this.requestId = requestId;
        this.requestedBy = requestedBy;
        this.premium = premium;
        this.includeCharts = includeCharts;
        this.attachPdf = attachPdf;
    }
}

