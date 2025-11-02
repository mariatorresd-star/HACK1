package com.example.hack1base.User.domain;


import com.example.hack1base.User.domain.Role;
import com.example.hack1base.User.domain.User;
import com.example.hack1base.User.estructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(Role role, String branch) {
        User u = new User();
        u.setUsername("alice");
        u.setEmail("alice@example.com");
        u.setPassword("Secret123!");
        u.setRole(role);
        u.setBranch(branch);
        return u;
    }

    @Test
    @DisplayName("Persiste CENTRAL válido sin branch")
    void shouldPersistCentralWithoutBranch() {
        User saved = userRepository.save(buildUser(Role.CENTRAL, null));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(Role.CENTRAL);
        assertThat(saved.getBranch()).isNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt())
                .isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Persiste BRANCH válido con branch")
    void shouldPersistBranchWithBranch() {
        User saved = userRepository.save(buildUser(Role.BRANCH, "LIMA-01"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(Role.BRANCH);
        assertThat(saved.getBranch()).isEqualTo("LIMA-01");
    }

    @Nested
    @DisplayName("Restricciones NOT NULL")
    class NotNullConstraints {

        @Test
        @DisplayName("Falla si username es null")
        void failWhenUsernameNull() {
            User u = buildUser(Role.CENTRAL, null);
            u.setUsername(null);
            assertThatThrownBy(() -> userRepository.saveAndFlush(u))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Falla si email es null")
        void failWhenEmailNull() {
            User u = buildUser(Role.CENTRAL, null);
            u.setEmail(null);
            assertThatThrownBy(() -> userRepository.saveAndFlush(u))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Falla si password es null")
        void failWhenPasswordNull() {
            User u = buildUser(Role.CENTRAL, null);
            u.setPassword(null);
            assertThatThrownBy(() -> userRepository.saveAndFlush(u))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Restricciones UNIQUE")
    class UniqueConstraints {

        @Test
        @DisplayName("Enforce unique email")
        void enforceUniqueEmail() {
            userRepository.save(buildUser(Role.CENTRAL, null));

            User dup = buildUser(Role.BRANCH, "SURCO-02");
            dup.setUsername("alice2");
            assertThatThrownBy(() -> userRepository.saveAndFlush(dup))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Enforce unique username")
        void enforceUniqueUsername() {
            userRepository.save(buildUser(Role.CENTRAL, null));

            User dup = buildUser(Role.BRANCH, "SURCO-02");
            dup.setEmail("alice2@example.com");
            assertThatThrownBy(() -> userRepository.saveAndFlush(dup))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    @DisplayName("Role se persiste/recupera correctamente (EnumType.STRING)")
    void roleIsStoredAndLoaded() {
        User saved = userRepository.save(buildUser(Role.BRANCH, "MIRA-03"));
        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getRole()).isEqualTo(Role.BRANCH);
    }

    @Test
    @DisplayName("createdAt se establece automáticamente al crear")
    void createdAtIsAutoInitialized() {
        User saved = userRepository.save(buildUser(Role.CENTRAL, null));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("username respeta longitud máxima (length=30)")
    void usernameLengthConstraint() {
        User u = buildUser(Role.CENTRAL, null);
        u.setUsername("x".repeat(31)); // 31 > 30
        assertThatThrownBy(() -> userRepository.saveAndFlush(u))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
