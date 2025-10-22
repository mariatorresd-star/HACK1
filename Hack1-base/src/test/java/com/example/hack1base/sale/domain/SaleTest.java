package com.example.hack1base.sale.domain;
import com.example.hack1base.Sale.domain.Sale;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class SaleTest {

    @PersistenceContext
    EntityManager em;

    // ------------ Helpers ------------

    private User newUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username.toLowerCase() + "@mail.com"); // dummy
        u.setRole(Role.BRANCH);
        u.setBranch("Miraflores");
        em.persist(u);
        return u;
    }

    private Sale newSaleViaSetters(User createdBy) {
        Sale s = new Sale();
        s.setSku("OREO_CLASSIC_12");
        s.setUnits(25);
        s.setPrice(1.99);
        s.setBranch("Miraflores");
        s.setSoldAt(LocalDateTime.of(2025, 9, 1, 10, 30));
        s.setCreatedBy(createdBy);
        return s;
    }

    // ------------ Tests ------------

    @Test
    @DisplayName("Persist genera ID y setea createdAt; datos básicos correctos")
    void persist_generatesId_and_setsCreatedAt() {
        User u = newUser("User1");
        Sale s = newSaleViaSetters(u);

        em.persist(s);
        em.flush();
        em.clear();

        Sale found = em.find(Sale.class, s.getId());

        // --- ID robusto: funciona si es String(UUID) o Long ---
        Object id = found.getId();
        assertThat(id).as("ID generado no debe ser null").isNotNull();

        if (id instanceof String str) {
            assertThat(str).isNotBlank();
            assertThatCode(() -> UUID.fromString(str)).doesNotThrowAnyException();
        } else if (id instanceof Long lng) {
            assertThat(lng).isPositive();
        } else {
            fail("Tipo de ID inesperado: " + id.getClass());
        }

        // createdAt se setea por inicializador de campo
        assertThat(found.getCreatedAt()).isNotNull();

        // datos básicos
        assertThat(found.getSku()).isEqualTo("OREO_CLASSIC_12");
        assertThat(found.getUnits()).isEqualTo(25);
        assertThat(found.getPrice()).isEqualTo(1.99d);
        assertThat(found.getBranch()).isEqualTo("Miraflores");
        assertThat(found.getSoldAt()).isEqualTo(LocalDateTime.of(2025, 9, 1, 10, 30));
    }

    @Test
    @DisplayName("sku nullable=false: sku=null provoca DataIntegrityViolationException")
    void sku_nullableFalse_violatesConstraint() {
        User u = newUser("User2");
        Sale s = newSaleViaSetters(u);
        s.setSku(null);

        em.persist(s);
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("branch nullable=false: branch=null provoca DataIntegrityViolationException")
    void branch_nullableFalse_violatesConstraint() {
        User u = newUser("User3");
        Sale s = newSaleViaSetters(u);
        s.setBranch(null);

        em.persist(s);
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("soldAt nullable=false: soldAt=null provoca DataIntegrityViolationException")
    void soldAt_nullableFalse_violatesConstraint() {
        User u = newUser("User4");
        Sale s = newSaleViaSetters(u);
        s.setSoldAt(null);

        em.persist(s);
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("createdBy nullable=false (FK): si es null, falla al flush")
    void createdBy_nullableFalse_violatesConstraint() {
        Sale s = newSaleViaSetters(null); // sin usuario

        em.persist(s);
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("@ManyToOne(fetch = LAZY) con User: no inicializa hasta acceder")
    void manyToOne_isLazy() {
        User u = newUser("User5");
        Sale s = newSaleViaSetters(u);
        em.persist(s);
        em.flush();
        em.clear();

        Sale found = em.find(Sale.class, s.getId());

        // Antes de acceder, el proxy debe estar no inicializado
        assertThat(Hibernate.isInitialized(found.getCreatedBy())).isFalse();

        // Al acceder, se inicializa
        String username = found.getCreatedBy().getUsername();
        assertThat(username).isEqualTo("User5");
        assertThat(Hibernate.isInitialized(found.getCreatedBy())).isTrue();
    }

    @Test
    @DisplayName("Actualizar campos mutables persiste cambios (units/price/sku)")
    void updatingMutableFields_persistsChanges() {
        User u = newUser("User6");
        Sale s = newSaleViaSetters(u);
        em.persist(s);
        em.flush();

        s.setUnits(40);
        s.setPrice(2.49);
        s.setSku("OREO_DOUBLE");
        em.merge(s);
        em.flush();
        em.clear();

        Sale reloaded = em.find(Sale.class, s.getId());
        assertThat(reloaded.getUnits()).isEqualTo(40);
        assertThat(reloaded.getPrice()).isEqualTo(2.49d);
        assertThat(reloaded.getSku()).isEqualTo("OREO_DOUBLE");
    }
}
