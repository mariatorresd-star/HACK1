package com.example.hack1base.events.application;

import com.example.hack1base.events.domain.ReportRequestedEvent;
import com.example.hack1base.events.dto.SummaryRequest;
import com.example.hack1base.JWT.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sales/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final ApplicationEventPublisher publisher;

    @PostMapping("/weekly")
    public ResponseEntity<Map<String, Object>> generateWeeklySummary(@RequestBody SummaryRequest request,
                                                                     Authentication auth) {

        if (request.getEmailTo() == null || request.getEmailTo().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "BAD_REQUEST", "message", "El campo emailTo es obligatorio"));
        }

        if (request.getFrom() == null || request.getTo() == null) {
            LocalDate today = LocalDate.now();
            request.setFrom(today.minusWeeks(1));
            request.setTo(today);
        }

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BRANCH"))) {
            if (request.getBranch() == null || request.getBranch().isBlank()) {
                return ResponseEntity.status(400).body(Map.of("error", "BAD_REQUEST", "message", "Branch es obligatorio para usuarios BRANCH"));
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof Account acc) {
                String userBranch = acc.getBranch();
                if (userBranch != null && !request.getBranch().equalsIgnoreCase(userBranch)) {
                    return ResponseEntity.status(403).body(Map.of("error", "FORBIDDEN", "message", "No puede solicitar resúmenes de otra sucursal"));
                }
            }
        }

        String requestId = UUID.randomUUID().toString();
        publisher.publishEvent(new ReportRequestedEvent(this, request, requestId, auth.getName()));

        Map<String, Object> response = Map.of(
                "requestId", requestId,
                "status", "PROCESSING",
                "message", "Su solicitud de reporte está siendo procesada. Recibirá el resumen en " + request.getEmailTo() + " en unos momentos.",
                "estimatedTime", "30-60 segundos",
                "requestedAt", Instant.now().toString()
        );

        return ResponseEntity.accepted().body(response);
    }
}

