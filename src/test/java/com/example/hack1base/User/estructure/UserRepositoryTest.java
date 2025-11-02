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
    @DisplayName("should persist user, assign ID, and initialize createdAt automatically")
    void shouldPersistUserAssignIdAndInitializeCreatedAt() {
        User toSave = buildUser("alice", "alice@example.com", Role.CENTRAL, null);

        User saved = userRepository.save(toSave);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getCreatedAt())
                .as("createdAt should be initialized on persist")
                .isNotNull();
        assertThat(saved.getCreatedAt())
                .isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
    }

    @Nested
    @DisplayName("existsByEmail / existsByUsername")
    class ExistenceChecks {

        @Test
        @DisplayName("should return true when email exists and false otherwise")
        void shouldReturnTrueWhenEmailExistsAndFalseOtherwise() {
            userRepository.save(buildUser("alice", "alice@example.com", Role.CENTRAL, null));

            assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nope@example.com")).isFalse();
        }

        @Test
        @DisplayName("should return true when username exists and false otherwise")
        void shouldReturnTrueWhenUsernameExistsAndFalseOtherwise() {
            userRepository.save(buildUser("bob", "bob@example.com", Role.BRANCH, "LIMA-01"));

            assertThat(userRepository.existsByUsername("bob")).isTrue();
            assertThat(userRepository.existsByUsername("charlie")).isFalse();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return entity when email exists")
        void shouldReturnEntityWhenEmailExists() {
            User saved = userRepository.save(buildUser("dora", "dora@example.com", Role.CENTRAL, null));

            User found = userRepository.findByEmail("dora@example.com");

            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getUsername()).isEqualTo("dora");
            assertThat(found.getRole()).isEqualTo(Role.CENTRAL);
        }

        @Test
        @DisplayName("should return null when email does not exist")
        void shouldReturnNullWhenEmailDoesNotExist() {
            User found = userRepository.findByEmail("missing@example.com");
            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("unique constraints (email and username)")
    class UniqueConstraints {

        @Test
        @DisplayName("should not allow duplicate email")
        void shouldNotAllowDuplicateEmail() {
            userRepository.saveAndFlush(buildUser("eva", "eva@example.com", Role.CENTRAL, null));

            User dupEmail = buildUser("eva2", "eva@example.com", Role.BRANCH, "SURCO-01");

            assertThatThrownBy(() -> userRepository.saveAndFlush(dupEmail))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("should not allow duplicate username")
        void shouldNotAllowDuplicateUsername() {
            userRepository.saveAndFlush(buildUser("frank", "frank@example.com", Role.CENTRAL, null));

            User dupUsername = buildUser("frank", "frank2@example.com", Role.BRANCH, "LIMA-02");

            assertThatThrownBy(() -> userRepository.saveAndFlush(dupUsername))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
