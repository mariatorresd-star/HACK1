package com.example.hack1base.ReportRequest.Application;

import com.example.hack1base.ReportRequest.application.ReportRequestController;
import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportRequestService;
import com.example.hack1base.ReportRequest.domain.ReportStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportRequestController.class)
class ReportRequestControllerTest {

    private static final String BASE_URL = "/api/report-requests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportRequestService reportRequestService;

    private ReportRequest buildReq(String branch, String email, ReportStatus status) {
        return ReportRequest.builder()
                .branch(branch)
                .fromDate(LocalDate.of(2025, 9, 1))
                .toDate(LocalDate.of(2025, 9, 7))
                .emailTo(email)
                .status(status)
                .message(null)
                .requestedBy(null)
                .build();
    }

    @Test
    @DisplayName("POST /api/report-requests -> 200 OK con cuerpo")
    void shouldCreateReportRequestReturnOkWithBody() throws Exception {
        ReportRequest payload = buildReq("Miraflores", "reports@demo.com", ReportStatus.PROCESSING);
        ReportRequest returned = buildReq("Miraflores", "reports@demo.com", ReportStatus.PROCESSING);

        when(reportRequestService.createReportRequest(any(ReportRequest.class))).thenReturn(returned);

        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.branch").value("Miraflores"))
                .andExpect(jsonPath("$.emailTo").value("reports@demo.com"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        verify(reportRequestService, times(1)).createReportRequest(any(ReportRequest.class));
        verifyNoMoreInteractions(reportRequestService);
    }

    @Test
    @DisplayName("GET /api/report-requests -> 200 OK lista")
    void  shouldReturnAllReportRequestsWithOkAndListBody() throws Exception {
        List<ReportRequest> list = List.of(
                buildReq("Miraflores", "mira@demo.com", ReportStatus.PROCESSING),
                buildReq("San Isidro", "si@demo.com", ReportStatus.COMPLETED)
        );
        when(reportRequestService.getAllReportRequests()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].branch").value("Miraflores"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));

        verify(reportRequestService, times(1)).getAllReportRequests();
        verifyNoMoreInteractions(reportRequestService);
    }

    @Test
    @DisplayName("GET /api/report-requests/{id} -> 200 OK cuando existe")
    void shouldReturnReportRequestByIdWhenExists() throws Exception {
        ReportRequest rr = buildReq("Miraflores", "reports@demo.com", ReportStatus.PROCESSING);
        when(reportRequestService.getReportRequestById(10L)).thenReturn(Optional.of(rr));

        mockMvc.perform(get(BASE_URL + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.branch").value("Miraflores"))
                .andExpect(jsonPath("$.emailTo").value("reports@demo.com"));

        verify(reportRequestService, times(1)).getReportRequestById(10L);
        verifyNoMoreInteractions(reportRequestService);
    }

    @Test
    @DisplayName("GET /api/report-requests/{id} -> 404 Not Found cuando no existe")
    void shouldReturnNotFoundWhenReportRequestDoesNotExist() throws Exception {
        when(reportRequestService.getReportRequestById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/{id}", 404L))
                .andExpect(status().isNotFound());

        verify(reportRequestService, times(1)).getReportRequestById(404L);
        verifyNoMoreInteractions(reportRequestService);
    }

    @Test
    @DisplayName("PUT /api/report-requests/{id} -> 200 OK con actualizado")
    void shouldUpdateReportRequestByIdReturnOkWithUpdatedBody() throws Exception {
        ReportRequest payload = buildReq("San Isidro", "new@demo.com", ReportStatus.COMPLETED);
        ReportRequest returned = buildReq("San Isidro", "new@demo.com", ReportStatus.COMPLETED);

        when(reportRequestService.updateReportRequest(eq(5L), any(ReportRequest.class)))
                .thenReturn(returned);

        mockMvc.perform(
                        put(BASE_URL + "/{id}", 5L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branch").value("San Isidro"))
                .andExpect(jsonPath("$.emailTo").value("new@demo.com"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(reportRequestService, times(1)).updateReportRequest(eq(5L), any(ReportRequest.class));
        verifyNoMoreInteractions(reportRequestService);
    }

    @Test
    @DisplayName("DELETE /api/report-requests/{id} -> 204 No Content")
    void shouldDeleteReportRequestByIdReturnNoContent() throws Exception {
        doNothing().when(reportRequestService).deleteReportRequest(77L);

        mockMvc.perform(delete(BASE_URL + "/{id}", 77L))
                .andExpect(status().isNoContent());

        verify(reportRequestService, times(1)).deleteReportRequest(77L);
        verifyNoMoreInteractions(reportRequestService);
    }
}
