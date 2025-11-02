package com.example.hack1base.ReportRequest.estructure;

import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportStatus;
import com.example.hack1base.ReportRequest.estructrure.ReportRequestRepository;
import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportRequestRepositoryTest {

    // -------- helper --------
    private ReportRequest mkReq(Long id, String branch, LocalDate from, LocalDate to,
                                String email, ReportStatus status, String msg) {
        return ReportRequest.builder()
                .id(id)
                .branch(branch)
                .fromDate(from)
                .toDate(to)
                .emailTo(email)
                .status(status)
                .message(msg)
                .requestedAt(LocalDateTime.of(2025, 1, 1, 9, 0))
                .requestedBy(mock(User.class))
                .build();
    }

    @Test
    @DisplayName("should save and return persisted entity")
    void shouldSaveAndReturnPersistedEntity() {

        ReportRequestRepository repo = mock(ReportRequestRepository.class);
        ReportRequest in = mkReq(null, "Miraflores",
                LocalDate.of(2025,1,1), LocalDate.of(2025,1,31),
                "r@corp.com", ReportStatus.PROCESSING, "mensual");
        when(repo.save(in)).thenReturn(in);

        ReportRequest out = repo.save(in);

        verify(repo).save(in);
        assertSame(in, out);
    }

    @Test
    @DisplayName("should return all report requests from repository")
    void shouldReturnAllReportRequestsFromRepository() {

        ReportRequestRepository repo = mock(ReportRequestRepository.class);
        List<ReportRequest> expected = List.of(
                mkReq(1L, "Surco", LocalDate.of(2025,2,1), LocalDate.of(2025,2,28),
                        "a@corp.com", ReportStatus.PROCESSING, null),
                mkReq(2L, "Lince", LocalDate.of(2025,3,1), LocalDate.of(2025,3,31),
                        "b@corp.com", ReportStatus.COMPLETED, "ok")
        );
        when(repo.findAll()).thenReturn(expected);

        List<ReportRequest> out = repo.findAll();

        verify(repo).findAll();
        assertEquals(expected, out);
    }

    @Test
    @DisplayName("should return optional with entity when id exists")
    void shouldReturnOptionalWithEntityWhenIdExists() {

        ReportRequestRepository repo = mock(ReportRequestRepository.class);
        ReportRequest entity = mkReq(10L, "Mira",
                LocalDate.of(2025,1,1), LocalDate.of(2025,1,31),
                "c@corp.com", ReportStatus.PROCESSING, null);
        when(repo.findById(10L)).thenReturn(Optional.of(entity));

        Optional<ReportRequest> out = repo.findById(10L);

        verify(repo).findById(10L);
        assertTrue(out.isPresent());
        assertSame(entity, out.get());
    }

    @Test
    @DisplayName("should delegate deletion by id")
    void shouldDelegateDeletionById() {

        ReportRequestRepository repo = mock(ReportRequestRepository.class);

        repo.deleteById(99L);

        verify(repo).deleteById(99L);
    }
}
