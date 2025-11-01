package com.example.hack1base.ReportRequest.Domain;


import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportStatus;

import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReportRequestTest {

    @Test
    @DisplayName("Builder: asigna correctamente todos los campos")
    void shouldBuildWithAllFields() {

        Long id = 1L;
        String branch = "Miraflores";
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        String emailTo = "reportes@empresa.com";
        ReportStatus status = ReportStatus.PROCESSING;
        String message = "Generando reporte mensual";
        LocalDateTime requestedAt = LocalDateTime.of(2025, 1, 1, 9, 0);
        User requestedBy = mock(User.class);


        ReportRequest req = ReportRequest.builder()
                .id(id)
                .branch(branch)
                .fromDate(from)
                .toDate(to)
                .emailTo(emailTo)
                .status(status)
                .message(message)
                .requestedAt(requestedAt)
                .requestedBy(requestedBy)
                .build();

        assertAll(
                () -> assertEquals(id, req.getId()),
                () -> assertEquals(branch, req.getBranch()),
                () -> assertEquals(from, req.getFromDate()),
                () -> assertEquals(to, req.getToDate()),
                () -> assertEquals(emailTo, req.getEmailTo()),
                () -> assertEquals(status, req.getStatus()),
                () -> assertEquals(message, req.getMessage()),
                () -> assertEquals(requestedAt, req.getRequestedAt()),
                () -> assertSame(requestedBy, req.getRequestedBy())
        );
    }

    @Test
    @DisplayName("NoArgsConstructor + setters: permite mutar valores")
    void shouldAllowMutationWithSetters() {
        ReportRequest req = new ReportRequest();
        User user = mock(User.class);
        LocalDateTime now = LocalDateTime.of(2025, 2, 2, 10, 30);


        req.setId(2L);
        req.setBranch("Surco");
        req.setFromDate(LocalDate.of(2025, 2, 1));
        req.setToDate(LocalDate.of(2025, 2, 28));
        req.setEmailTo("destino@empresa.com");
        req.setStatus(ReportStatus.COMPLETED);
        req.setMessage("Listo");
        req.setRequestedAt(now);
        req.setRequestedBy(user);


        assertAll(
                () -> assertEquals(2L, req.getId()),
                () -> assertEquals("Surco", req.getBranch()),
                () -> assertEquals(LocalDate.of(2025, 2, 1), req.getFromDate()),
                () -> assertEquals(LocalDate.of(2025, 2, 28), req.getToDate()),
                () -> assertEquals("destino@empresa.com", req.getEmailTo()),
                () -> assertEquals(ReportStatus.COMPLETED, req.getStatus()),
                () -> assertEquals("Listo", req.getMessage()),
                () -> assertEquals(now, req.getRequestedAt()),
                () -> assertSame(user, req.getRequestedBy())
        );
    }

    @Test
    @DisplayName("Valores por defecto: status=PROCESSING, requestedAt cercano a now, message=null")
    void shouldInitializeDefaults() {

        LocalDateTime before = LocalDateTime.now().minusSeconds(3);


        ReportRequest req = new ReportRequest();

        assertAll(
                () -> assertEquals(ReportStatus.PROCESSING, req.getStatus(), "status por defecto"),
                () -> assertNull(req.getMessage(), "message es opcional y debe iniciar en null"),
                () -> {
                    assertNotNull(req.getRequestedAt(), "requestedAt no debe ser null");
                    LocalDateTime after = LocalDateTime.now().plusSeconds(3);
                    assertTrue(!req.getRequestedAt().isBefore(before) && !req.getRequestedAt().isAfter(after),
                            "requestedAt debe ser cercano a now");
                }
        );
    }

    @Test
    @DisplayName("Permite valores límite: message null y fechas iguales (from==to)")
    void shouldAcceptBoundaryValues() {

        ReportRequest req = ReportRequest.builder()
                .branch("Lince")
                .fromDate(LocalDate.of(2025, 3, 15))
                .toDate(LocalDate.of(2025, 3, 15))
                .emailTo("x@empresa.com")
                .requestedBy(mock(User.class))
                .build();


        assertAll(
                () -> assertEquals(LocalDate.of(2025, 3, 15), req.getFromDate()),
                () -> assertEquals(LocalDate.of(2025, 3, 15), req.getToDate()),
                () -> assertNull(req.getMessage()),
                () -> assertEquals(ReportStatus.PROCESSING, req.getStatus()),
                () -> assertNotNull(req.getRequestedAt())
        );
    }
}
