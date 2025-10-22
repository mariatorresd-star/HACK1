package com.example.hack1base.ReportRequest.Domain;

import com.example.hack1base.ReportRequest.domain.ReportRequest;
import com.example.hack1base.ReportRequest.domain.ReportStatus;
import com.example.hack1base.User.domain.Role;
import com.example.hack1base.User.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ReportRequestTest {

    @PersistenceContext
    EntityManager em;

    // ----------------- Helpers -----------------

    private User newUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username.toLowerCase() + "@mail.com");
        u.setRole(Role.BRANCH);
        u.setBranch("Miraflores");
        em.persist(u);
        return u;
    }

    private ReportRequest newRequestViaSetters(User requestedBy) {
        ReportRequest r = new ReportRequest();
        r.setBranch("Miraflores");
        r.setFromDate(LocalDate.of(2025, 9, 1));
        r.setToDate(LocalDate.of(2025, 9, 7));
        r.setEmailTo("reports@demo.com");
        // No fijamos status ni requestedAt para verificar defaults del campo
        r.setRequestedBy(requestedBy);
        return r;
    }

    private ReportRequest newRequestViaBuilder(User requestedBy) {
        // Con @Builder, los inicializadores de campo NO aplican salvo @Builder.Default:
        // por eso aquí seteamos explícitamente status y requestedAt.
        return ReportRequest.builder()
                .branch("Miraflores")
                .fromDate(LocalDate.of(2025, 9, 1))
                .toDate(LocalDate.of(2025, 9, 7))
                .emailTo("reports@demo.com")
                .status(ReportStatus.PROCESSING)
                .requestedAt(LocalDateTime.now())
                .requestedBy(requestedBy)
                .build();
    }

    // ----------------- Tests -----------------

    @Test
    @DisplayName("Persist (via setters) aplica defaults: status=PROCESSING y requestedAt!=null; genera UUID String")
    void persistWithSetters_appliesDefaultsAndGeneratesUuid() {
        User u = newUser("User1");

        ReportRequest r = newRequestViaSetters(u);
        em.persist(r);
        em.flush();
        em.clear();

        ReportRequest found = em.find(ReportRequest.class, r.getId());

        // ID UUID (String)
        assertThat(found.getId()).isNotBlank();
        assertThatCode(() -> UUID.fromString(found.getId())).doesNotThrowAnyException();

        // Defaults
        assertThat(found.getStatus()).isEqualTo(ReportStatus.PROCESSING);
        assertThat(found.getRequestedAt()).isNotNull();

        // Datos básicos
        assertThat(found.getBranch()).isEqualTo("Miraflores");
        assertThat(found.getFromDate()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(found.getToDate()).isEqualTo(LocalDate.of(2025, 9, 7));
        assertThat(found.getEmailTo()).isEqualTo("reports@demo.com");
    }

    @Test
    @DisplayName("Persist (via builder) funciona si se setean explícitamente status y requestedAt")
    void persistWithBuilder_okWhenSettingDefaultsExplicitly() {
        User u = newUser("User2");

        ReportRequest r = newRequestViaBuilder(u);
        em.persist(r);
        em.flush();
        em.clear();

        ReportRequest found = em.find(ReportRequest.class, r.getId());
        assertThat(found.getStatus()).isEqualTo(ReportStatus.PROCESSING);
        assertThat(found.getRequestedAt()).isNotNull();
    }

    @Test
    @DisplayName("branch nullable=false: branch=null provoca DataIntegrityViolationException")
    void branchNullableFalse_violatesConstraint() {
        User u = newUser("User3");

        ReportRequest r = newRequestViaSetters(u);
        r.setBranch(null);

        em.persist(r);
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FK requestedBy nullable=false: si es null, falla al flush")
    void requestedByNullableFalse_violatesConstraint() {
        ReportRequest r = newRequestViaSetters(null); // sin usuario

        em.persist(r);
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("requestedAt es updatable=false: un cambio manual no debe persistirse")
    void requestedAt_isNotUpdatable() {
        User u = newUser("User4");
        ReportRequest r = newRequestViaSetters(u);
        em.persist(r);
        em.flush();

        LocalDateTime original = r.getRequestedAt();
        r.setRequestedAt(original.minusDays(5)); // intento de mutar
        em.merge(r);
        em.flush();
        em.clear();

        ReportRequest reloaded = em.find(ReportRequest.class, r.getId());
        // Debe permanecer con el valor original
        assertThat(reloaded.getRequestedAt()).isEqualTo(original);
    }

    @Test
    @DisplayName("Enum se persiste como STRING: valor textual 'COMPLETED' en la base")
    void enumStoredAsString() {
        User u = newUser("User5");
        ReportRequest r = newRequestViaSetters(u);
        em.persist(r);
        em.flush();

        r.setStatus(ReportStatus.COMPLETED);
        em.merge(r);
        em.flush();

        String sql = "SELECT status FROM report_requests WHERE id = :id";
        Object single = em.createNativeQuery(sql)
                .setParameter("id", r.getId())
                .getSingleResult();

        assertThat(single).isInstanceOf(String.class);
        assertThat((String) single).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("@ManyToOne(fetch = LAZY) con User: no inicializa hasta acceder")
    void manyToOne_isLazy() {
        User u = newUser("User6");
        ReportRequest r = newRequestViaSetters(u);
        em.persist(r);
        em.flush();
        em.clear();

        ReportRequest found = em.find(ReportRequest.class, r.getId());

        // En contexto transaccional, inicialmente no debe estar inicializado
        assertThat(Hibernate.isInitialized(found.getRequestedBy())).isFalse();

        // Accedemos -> se inicializa
        String username = found.getRequestedBy().getUsername();
        assertThat(username).isEqualTo("User6");
        assertThat(Hibernate.isInitialized(found.getRequestedBy())).isTrue();
    }
}
