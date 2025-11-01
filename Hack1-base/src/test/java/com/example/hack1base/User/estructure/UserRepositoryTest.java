package com.example.hack1base.User.estructure;


import com.example.hack1base.User.domain.Role;
import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
// Si usas Testcontainers, agrega tu @Testcontainers/@Container y configura datasource.
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static User buildUser(String username, String email, Role role, String branch) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("Secret123!");
        u.setRole(role);
        u.setBranch(branch);
        return u;
    }

    @Test
    @DisplayName("save: persiste un usuario y asigna ID; createdAt no es nulo si se gestiona automáticamente")
    void save_persistsUser() {
        User toSave = buildUser("alice", "alice@example.com", Role.CENTRAL, null);

        User saved = userRepository.save(toSave);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        // Si tu entidad inicializa createdAt (via @PrePersist o default DB), esto será no nulo.
        assertThat(saved.getCreatedAt())
                .as("createdAt debería inicializarse en persist")
                .isNotNull();
        assertThat(saved.getCreatedAt())
                .isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
    }

    @Nested
    @DisplayName("existsByEmail / existsByUsername")
    class ExistenceChecks {

        @Test
        @DisplayName("existsByEmail: true cuando el email existe; false cuando no")
        void existsByEmail_trueFalse() {
            userRepository.save(buildUser("alice", "alice@example.com", Role.CENTRAL, null));

            assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nope@example.com")).isFalse();
        }

        @Test
        @DisplayName("existsByUsername: true cuando el username existe; false cuando no")
        void existsByUsername_trueFalse() {
            userRepository.save(buildUser("bob", "bob@example.com", Role.BRANCH, "LIMA-01"));

            assertThat(userRepository.existsByUsername("bob")).isTrue();
            assertThat(userRepository.existsByUsername("charlie")).isFalse();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("findByEmail: devuelve la entidad cuando existe")
        void findByEmail_returnsEntity() {
            User saved = userRepository.save(buildUser("dora", "dora@example.com", Role.CENTRAL, null));

            User found = userRepository.findByEmail("dora@example.com");

            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getUsername()).isEqualTo("dora");
            assertThat(found.getRole()).isEqualTo(Role.CENTRAL);
        }

        @Test
        @DisplayName("findByEmail: devuelve null cuando no existe")
        void findByEmail_returnsNullWhenNotFound() {
            User found = userRepository.findByEmail("missing@example.com");
            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("Restricciones de unicidad (email y username)")
    class UniqueConstraints {

        @Test
        @DisplayName("No permite duplicar email")
        void uniqueEmail() {
            userRepository.saveAndFlush(buildUser("eva", "eva@example.com", Role.CENTRAL, null));

            User dupEmail = buildUser("eva2", "eva@example.com", Role.BRANCH, "SURCO-01");

            assertThatThrownBy(() -> userRepository.saveAndFlush(dupEmail))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("No permite duplicar username")
        void uniqueUsername() {
            userRepository.saveAndFlush(buildUser("frank", "frank@example.com", Role.CENTRAL, null));

            User dupUsername = buildUser("frank", "frank2@example.com", Role.BRANCH, "LIMA-02");

            assertThatThrownBy(() -> userRepository.saveAndFlush(dupUsername))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
