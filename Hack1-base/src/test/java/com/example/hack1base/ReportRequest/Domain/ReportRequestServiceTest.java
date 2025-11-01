package com.example.hack1base.ReportRequest.Domain;

import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportRequestService;
import com.example.hack1base.ReportRequest.domain.ReportStatus;
import com.example.hack1base.ReportRequest.estructrure.ReportRequestRepository;
import com.example.hack1base.User.domain.Role;
import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportRequestServiceTest {

    @Mock
    private ReportRequestRepository reportRequestRepository;

    @InjectMocks
    private ReportRequestService reportRequestService;

    private ReportRequest base;
    private User requester;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setUsername("miraflores.user");
        requester.setEmail("miraflores.user@mail.com");
        requester.setRole(Role.BRANCH);
        requester.setBranch("Miraflores");

        base = ReportRequest.builder()
                .branch("Miraflores")
                .fromDate(LocalDate.of(2025, 9, 1))
                .toDate(LocalDate.of(2025, 9, 7))
                .emailTo("reports@demo.com")
                .status(ReportStatus.PROCESSING)
                .message(null)
                .requestedBy(requester)
                .build();
    }

    @Test
    @DisplayName("should save report request and return saved entity")
    void shouldSaveReportRequestAndReturnSavedEntity() {
        when(reportRequestRepository.save(any(ReportRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReportRequest saved = reportRequestService.createReportRequest(base);

        assertThat(saved).isNotNull();
        assertThat(saved.getBranch()).isEqualTo("Miraflores");
        assertThat(saved.getFromDate()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(saved.getToDate()).isEqualTo(LocalDate.of(2025, 9, 7));
        assertThat(saved.getEmailTo()).isEqualTo("reports@demo.com");
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.PROCESSING);

        verify(reportRequestRepository, times(1)).save(any(ReportRequest.class));
        verifyNoMoreInteractions(reportRequestRepository);
    }

    @Test
    @DisplayName("should return all report requests from repository")
    void shouldReturnAllReportRequestsFromRepository() {
        ReportRequest other = ReportRequest.builder()
                .branch("San Isidro")
                .fromDate(LocalDate.of(2025, 9, 1))
                .toDate(LocalDate.of(2025, 9, 7))
                .emailTo("si@demo.com")
                .status(ReportStatus.PROCESSING)
                .requestedBy(requester)
                .build();

        when(reportRequestRepository.findAll()).thenReturn(List.of(base, other));

        List<ReportRequest> all = reportRequestService.getAllReportRequests();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(ReportRequest::getBranch)
                .containsExactly("Miraflores", "San Isidro");

        verify(reportRequestRepository, times(1)).findAll();
        verifyNoMoreInteractions(reportRequestRepository);
    }

    @Test
    @DisplayName("should return report request by id when it exists")
    void shouldReturnReportRequestByIdWhenExists() {
        when(reportRequestRepository.findById(10L)).thenReturn(Optional.of(base));

        Optional<ReportRequest> found = reportRequestService.getReportRequestById(10L);

        assertThat(found).isPresent();
        assertThat(found.get().getEmailTo()).isEqualTo("reports@demo.com");

        verify(reportRequestRepository, times(1)).findById(10L);
        verifyNoMoreInteractions(reportRequestRepository);
    }

    @Test
    @DisplayName("should return empty optional when report request does not exist")
    void shouldReturnEmptyOptionalWhenReportRequestDoesNotExist() {
        when(reportRequestRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ReportRequest> found = reportRequestService.getReportRequestById(999L);

        assertThat(found).isEmpty();
        verify(reportRequestRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(reportRequestRepository);
    }

    @Test
    @DisplayName("should update fields and save report request")
    void shouldUpdateFieldsAndSaveReportRequest() {
        ReportRequest existing = ReportRequest.builder()
                .branch("Miraflores")
                .fromDate(LocalDate.of(2025, 9, 1))
                .toDate(LocalDate.of(2025, 9, 7))
                .emailTo("old@demo.com")
                .status(ReportStatus.PROCESSING)
                .message(null)
                .requestedBy(requester)
                .build();

        when(reportRequestRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(reportRequestRepository.save(any(ReportRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // datos nuevos
        ReportRequest updatedData = ReportRequest.builder()
                .branch("San Isidro")
                .fromDate(LocalDate.of(2025, 9, 2))
                .toDate(LocalDate.of(2025, 9, 8))
                .emailTo("new@demo.com")
                .status(ReportStatus.COMPLETED)
                .message("OK")
                .requestedBy(requester) // el service no lo toca, pero no estorba
                .build();

        ReportRequest result = reportRequestService.updateReportRequest(5L, updatedData);

        ArgumentCaptor<ReportRequest> captor = ArgumentCaptor.forClass(ReportRequest.class);
        verify(reportRequestRepository, times(1)).findById(5L);
        verify(reportRequestRepository, times(1)).save(captor.capture());
        verifyNoMoreInteractions(reportRequestRepository);

        ReportRequest toSave = captor.getValue();
        assertThat(toSave.getBranch()).isEqualTo("San Isidro");
        assertThat(toSave.getFromDate()).isEqualTo(LocalDate.of(2025, 9, 2));
        assertThat(toSave.getToDate()).isEqualTo(LocalDate.of(2025, 9, 8));
        assertThat(toSave.getEmailTo()).isEqualTo("new@demo.com");
        assertThat(toSave.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(toSave.getMessage()).isEqualTo("OK");

        // el método devuelve lo que guarda el repo
        assertThat(result.getStatus()).isEqualTo(ReportStatus.COMPLETED);
    }

    @Test
    @DisplayName("should throw when updating non-existing report request")
    void shouldThrowWhenUpdatingNonExistingReportRequest() {
        when(reportRequestRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportRequestService.updateReportRequest(404L, base))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ReportRequest not found");

        verify(reportRequestRepository, times(1)).findById(404L);
        verifyNoMoreInteractions(reportRequestRepository);
    }

    @Test
    @DisplayName("should delete report request by id")
    void shouldDeleteReportRequestById() {
        doNothing().when(reportRequestRepository).deleteById(77L);

        reportRequestService.deleteReportRequest(77L);

        verify(reportRequestRepository, times(1)).deleteById(77L);
        verifyNoMoreInteractions(reportRequestRepository);
    }
}
