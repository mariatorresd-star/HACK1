package com.example.hack1base.ReportRequest.Infraestructure;


import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportStatus;
import com.example.hack1base.ReportRequest.estructrure.ReportRequestRepository;
import com.example.hack1base.User.domain.Role;
import com.example.hack1base.User.domain.User;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ReportRequestRepositoryTest {

    @Autowired
    private ReportRequestRepository reportRequestRepository;

    @Autowired
    private EntityManager em;

    // ---------- helpers ----------

    private User newUser(String username, String branch) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@mail.com");
        u.setRole(Role.BRANCH);
        u.setBranch(branch);
        em.persist(u);
        return u;
    }

    private ReportRequest newReq(String branch, String emailTo, LocalDate from, LocalDate to, User by) {
        ReportRequest r = new ReportRequest();
        r.setBranch(branch);
        r.setEmailTo(emailTo);
        r.setFromDate(from);
        r.setToDate(to);
        // no seteamos status ni requestedAt para comprobar defaults (PROCESSING + now)
        r.setRequestedBy(by);
        return r;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("save + findById: persiste, genera UUID String, respeta defaults y recupera")
    void save_and_findById() {
        User by = newUser("miraflores.user", "Miraflores");
        ReportRequest req = newReq("Miraflores", "reports@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by);

        ReportRequest saved = reportRequestRepository.save(req);

        // ID (String UUID)
        assertThat(saved.getId()).isNotNull();
        assertThatCode(() -> UUID.fromString(saved.getId())).doesNotThrowAnyException();

        // defaults
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.PROCESSING);
        assertThat(saved.getRequestedAt()).isNotNull();

        Optional<ReportRequest> found = reportRequestRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getBranch()).isEqualTo("Miraflores");
        assertThat(found.get().getEmailTo()).isEqualTo("reports@demo.com");
    }

    @Test
    @DisplayName("findAll + sort/pagination: ordena por requestedAt DESC y pagina")
    void findAll_sort_and_pagination() {
        User by = newUser("user1", "Miraflores");

        ReportRequest r1 = newReq("Miraflores", "a@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by);
        ReportRequest r2 = newReq("San Isidro", "b@demo.com",
                LocalDate.of(2025, 9, 2), LocalDate.of(2025, 9, 8), by);
        ReportRequest r3 = newReq("Barranco", "c@demo.com",
                LocalDate.of(2025, 9, 3), LocalDate.of(2025, 9, 9), by);

        reportRequestRepository.saveAll(List.of(r1, r2, r3));

        Page<ReportRequest> page = reportRequestRepository.findAll(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "requestedAt"))
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        // r3 y r2 deberían estar primero (más recientes)
        assertThat(page.getContent().get(0).getBranch()).isIn("Barranco", "San Isidro");
    }

    @Test
    @DisplayName("update: cambia status y message, persiste y recarga")
    void update_status_and_message() {
        User by = newUser("user2", "Miraflores");
        ReportRequest r = reportRequestRepository.save(newReq(
                "Miraflores", "x@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by));

        r.setStatus(ReportStatus.COMPLETED);
        r.setMessage("Resumen enviado");
        reportRequestRepository.saveAndFlush(r);
        em.clear();

        ReportRequest reloaded = reportRequestRepository.findById(r.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(reloaded.getMessage()).isEqualTo("Resumen enviado");
    }

    @Test
    @DisplayName("deleteById elimina el registro")
    void deleteById_removesRow() {
        User by = newUser("user3", "Miraflores");
        ReportRequest r = reportRequestRepository.save(newReq(
                "Miraflores", "del@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by));

        assertThat(reportRequestRepository.findById(r.getId())).isPresent();

        reportRequestRepository.deleteById(r.getId());

        assertThat(reportRequestRepository.findById(r.getId())).isNotPresent();
    }

    @Test
    @DisplayName("constraints: nullable=false en branch/fromDate/toDate/emailTo/requestedBy")
    void constraints_nullableFalse() {
        User by = newUser("user4", "Miraflores");

        // branch null
        ReportRequest r1 = newReq(null, "a@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by);
        assertThatThrownBy(() -> reportRequestRepository.saveAndFlush(r1))
                .isInstanceOf(DataIntegrityViolationException.class);

        // fromDate null
        ReportRequest r2 = newReq("Miraflores", "b@demo.com",
                null, LocalDate.of(2025, 9, 7), by);
        assertThatThrownBy(() -> reportRequestRepository.saveAndFlush(r2))
                .isInstanceOf(DataIntegrityViolationException.class);

        // toDate null
        ReportRequest r3 = newReq("Miraflores", "c@demo.com",
                LocalDate.of(2025, 9, 1), null, by);
        assertThatThrownBy(() -> reportRequestRepository.saveAndFlush(r3))
                .isInstanceOf(DataIntegrityViolationException.class);

        // emailTo null
        ReportRequest r4 = newReq("Miraflores", null,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by);
        assertThatThrownBy(() -> reportRequestRepository.saveAndFlush(r4))
                .isInstanceOf(DataIntegrityViolationException.class);

        // requestedBy null
        ReportRequest r5 = newReq("Miraflores", "ok@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), null);
        assertThatThrownBy(() -> reportRequestRepository.saveAndFlush(r5))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("@ManyToOne LAZY: requestedBy no inicializa hasta acceder")
    void manyToOne_lazy_requestedBy() {
        User by = newUser("user5", "Miraflores");
        ReportRequest r = reportRequestRepository.save(newReq(
                "Miraflores", "lazy@demo.com",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 7), by));

        em.flush();
        em.clear();

        ReportRequest reloaded = reportRequestRepository.findById(r.getId()).orElseThrow();
        assertThat(Hibernate.isInitialized(reloaded.getRequestedBy())).isFalse();

        String username = reloaded.getRequestedBy().getUsername(); // fuerza carga
        assertThat(username).isEqualTo("user5");
        assertThat(Hibernate.isInitialized(reloaded.getRequestedBy())).isTrue();
    }
}
