package com.example.hack1base.User.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class UserResponseTest {

    @Test
    @DisplayName("should preserve values via getters and setters")
    void shouldPreserveValuesViaGettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();

        UserResponse dto = new UserResponse();
        dto.setId("1");
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setRole("CENTRAL");
        dto.setBranch(null);
        dto.setCreatedAt(now);

        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getUsername()).isEqualTo("alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getRole()).isEqualTo("CENTRAL");
        assertThat(dto.getBranch()).isNull();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Nested
    @DisplayName("equals / hashCode / toString (@Data)")
    class EqualityAndToString {

        @Test
        @DisplayName("should consider equals/hashCode equal for same fields and different when id differs")
        void shouldConsiderEqualsAndHashCodeForSameFieldsAndDifferentWhenIdDiffers() {
            LocalDateTime t = LocalDateTime.of(2025, 1, 1, 12, 0);

            UserResponse a = new UserResponse();
            a.setId("1");
            a.setUsername("alice");
            a.setEmail("alice@example.com");
            a.setRole("CENTRAL");
            a.setBranch(null);
            a.setCreatedAt(t);

            UserResponse b = new UserResponse();
            b.setId("1");
            b.setUsername("alice");
            b.setEmail("alice@example.com");
            b.setRole("CENTRAL");
            b.setBranch(null);
            b.setCreatedAt(t);

            UserResponse c = new UserResponse();
            c.setId("2");
            c.setUsername("alice");
            c.setEmail("alice@example.com");
            c.setRole("CENTRAL");
            c.setBranch(null);
            c.setCreatedAt(t);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
            assertThat(a).isNotEqualTo(c);
        }

        @Test
        @DisplayName("should include class name and key fields in toString")
        void shouldIncludeClassNameAndKeyFieldsInToString() {
            UserResponse dto = new UserResponse();
            dto.setId("1");
            dto.setUsername("alice");
            dto.setEmail("alice@example.com");
            dto.setRole("CENTRAL");

            String s = dto.toString();
            assertThat(s).contains("UserResponse");
            assertThat(s).contains("alice");
            assertThat(s).contains("alice@example.com");
            assertThat(s).contains("CENTRAL");
        }
    }

    @Test
    @DisplayName("should map User to UserResponse with ModelMapper (Long→String, Enum→String)")
    void shouldMapUserToUserResponseWithModelMapper() {
        User user = new User();
        user.setId(123L);
        user.setUsername("bob");
        user.setEmail("bob@example.com");
        user.setPassword("ENCODED");
        user.setRole(Role.BRANCH);
        user.setBranch("LIMA-01");
        user.setCreatedAt(LocalDateTime.of(2025, 10, 10, 10, 10));

        ModelMapper mapper = new ModelMapper();

        UserResponse dto = mapper.map(user, UserResponse.class);

        assertThat(dto.getId()).isEqualTo("123");
        assertThat(dto.getUsername()).isEqualTo("bob");
        assertThat(dto.getEmail()).isEqualTo("bob@example.com");
        assertThat(dto.getRole()).isEqualTo("BRANCH");   // Enum → String
        assertThat(dto.getBranch()).isEqualTo("LIMA-01");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 10, 10, 10, 10));
    }
}
