package com.example.hack1base.ReportRequest.application;

import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-requests")
@RequiredArgsConstructor
public class ReportRequestController {

    private final ReportRequestService reportRequestService;

    @PostMapping
    public ResponseEntity<ReportRequest> create(@RequestBody ReportRequest reportRequest) {
        return ResponseEntity.ok(reportRequestService.createReportRequest(reportRequest));
    }

    @GetMapping
    public ResponseEntity<List<ReportRequest>> getAll() {
        return ResponseEntity.ok(reportRequestService.getAllReportRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportRequest> getById(@PathVariable Long id) {
        return reportRequestService.getReportRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportRequest> update(
            @PathVariable Long id,
            @RequestBody ReportRequest reportRequest) {
        return ResponseEntity.ok(reportRequestService.updateReportRequest(id, reportRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportRequestService.deleteReportRequest(id);
        return ResponseEntity.noContent().build();
    }
}