package com.example.hack1base.sale.infraestructure;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.estructure.SaleRepository;
import com.example.hack1base.User.domain.Role;   // si en tu proyecto no usas enum, cambia a String
import com.example.hack1base.User.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SaleRepositoryTest {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private EntityManager em;

    // ---------------- helpers ----------------

    private User newUser(String username, String branch) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@mail.com");
        u.setRole(Role.BRANCH); // si es String en tu entidad: u.setRole("BRANCH");
        u.setBranch(branch);
        em.persist(u);
        return u;
    }

    private Sale newSale(String sku, int units, double price, String branch, LocalDateTime soldAt, User createdBy) {
        Sale s = new Sale();
        s.setSku(sku);
        s.setUnits(units);
        s.setPrice(price);
        s.setBranch(branch);
        s.setSoldAt(soldAt);
        s.setCreatedBy(createdBy);
        return s;
    }

    // ---------------- tests ----------------

    @Test
    @DisplayName("save + findById: persiste y recupera por ID (soporta ID Long o UUID String)")
    void save_and_findById() {
        User u = newUser("miraflores.user", "Miraflores");
        Sale s = newSale("OREO_CLASSIC_12", 25, 1.99, "Miraflores",
                LocalDateTime.of(2025, 9, 12, 16, 30), u);

        Sale saved = saleRepository.save(s);

        // Verificación robusta del ID (Long o String UUID)
        Object id = saved.getId();
        assertThat(id).isNotNull();
        if (id instanceof Long l) {
            assertThat(l).isPositive();
        } else if (id instanceof String str) {
            assertThat(str).isNotBlank();
            assertThatCode(() -> UUID.fromString(str)).doesNotThrowAnyException();
        } else {
            fail("Tipo de ID inesperado: " + id.getClass());
        }

        // findById usando el mismo tipo de id real
        Optional<Sale> found = saleRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSku()).isEqualTo("OREO_CLASSIC_12");
        assertThat(found.get().getCreatedBy().getUsername()).isEqualTo("miraflores.user");
    }

    @Test
    @DisplayName("findAll + sort/pagination: ordena por soldAt desc y pagina")
    void findAll_withSorting_andPagination() {
        User u = newUser("user1", "Miraflores");

        Sale s1 = newSale("A", 10, 1.0, "Miraflores", LocalDateTime.of(2025, 9, 1, 10, 0), u);
        Sale s2 = newSale("B", 20, 2.0, "Miraflores", LocalDateTime.of(2025, 9, 2, 10, 0), u);
        Sale s3 = newSale("C", 30, 3.0, "Miraflores", LocalDateTime.of(2025, 9, 3, 10, 0), u);
        saleRepository.saveAll(List.of(s1, s2, s3));

        Page<Sale> page = saleRepository.findAll(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "soldAt"))
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        // Debe venir primero el más reciente (s3), luego s2
        assertThat(page.getContent().get(0).getSku()).isEqualTo("C");
        assertThat(page.getContent().get(1).getSku()).isEqualTo("B");
    }

    @Test
    @DisplayName("findAll: retorna todas las ventas")
    void findAll_returnsAll() {
        User u = newUser("user2", "Miraflores");
        saleRepository.save(newSale("X", 5, 0.5, "Miraflores", LocalDateTime.now(), u));
        saleRepository.save(newSale("Y", 6, 0.6, "Miraflores", LocalDateTime.now().minusHours(1), u));

        List<Sale> all = saleRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Sale::getSku).containsExactlyInAnyOrder("X", "Y");
    }

    @Test
    @DisplayName("deleteById elimina el registro")
    void deleteById_removesRow() {
        User u = newUser("user3", "Miraflores");
        Sale s = saleRepository.save(newSale("DEL", 1, 0.1, "Miraflores", LocalDateTime.now(), u));

        assertThat(saleRepository.findById(s.getId())).isPresent();

        saleRepository.deleteById(s.getId());

        assertThat(saleRepository.findById(s.getId())).isNotPresent();
    }

    @Test
    @DisplayName("constraints: nullable=false en sku/branch/soldAt/createdBy")
    void constraints_nullableFalse() {
        User u = newUser("user4", "Miraflores");

        // sku null
        Sale s1 = newSale(null, 10, 1.0, "Miraflores", LocalDateTime.now(), u);
        assertThatThrownBy(() -> saleRepository.saveAndFlush(s1))
                .isInstanceOf(DataIntegrityViolationException.class);

        // branch null
        Sale s2 = newSale("SKU", 10, 1.0, null, LocalDateTime.now(), u);
        assertThatThrownBy(() -> saleRepository.saveAndFlush(s2))
                .isInstanceOf(DataIntegrityViolationException.class);

        // soldAt null
        Sale s3 = newSale("SKU", 10, 1.0, "Miraflores", null, u);
        assertThatThrownBy(() -> saleRepository.saveAndFlush(s3))
                .isInstanceOf(DataIntegrityViolationException.class);

        // createdBy null
        Sale s4 = newSale("SKU", 10, 1.0, "Miraflores", LocalDateTime.now(), null);
        assertThatThrownBy(() -> saleRepository.saveAndFlush(s4))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
