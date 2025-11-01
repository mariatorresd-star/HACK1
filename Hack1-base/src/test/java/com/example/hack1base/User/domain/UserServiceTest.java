package com.example.hack1base.User.domain;
import com.example.hack1base.User.estructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.modelmapper.ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User userCentral;
    private UserResponse userResponseCentral;

    @BeforeEach
    void setUp() {
        userCentral = new User();
        userCentral.setId(1L);
        userCentral.setUsername("alice");
        userCentral.setEmail("alice@example.com");
        userCentral.setPassword("Secret123!");
        userCentral.setRole(Role.CENTRAL);
        userCentral.setBranch(null);

        userResponseCentral = new UserResponse();
        userResponseCentral.setId("1");
        userResponseCentral.setUsername("alice");
        userResponseCentral.setEmail("alice@example.com");
        userResponseCentral.setRole("CENTRAL");
        userResponseCentral.setBranch(null);
    }

    private User cloneUser(User u) {
        User c = new User();
        c.setId(u.getId());
        c.setUsername(u.getUsername());
        c.setEmail(u.getEmail());
        c.setPassword(u.getPassword());
        c.setRole(u.getRole());
        c.setBranch(u.getBranch());
        c.setCreatedAt(u.getCreatedAt());
        return c;
    }

    // ---------- REGISTER ----------

    @Test
    @DisplayName("register: crea usuario cuando email/username no existen, encripta y mapea a UserResponse (id/role String)")
    void register_success() {
        User toSave = cloneUser(userCentral);
        toSave.setId(null); // simulamos nuevo

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("ENCODED");

        User saved = cloneUser(userCentral);
        saved.setPassword("ENCODED");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        when(modelMapper.map(saved, UserResponse.class)).thenReturn(userResponseCentral);

        UserResponse resp = userService.register(toSave);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo("1");
        assertThat(resp.getUsername()).isEqualTo("alice");
        assertThat(resp.getEmail()).isEqualTo("alice@example.com");
        assertThat(resp.getRole()).isEqualTo("CENTRAL");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("ENCODED");

        verify(userRepository).existsByEmail("alice@example.com");
        verify(userRepository).existsByUsername("alice");
        verify(passwordEncoder).encode("Secret123!");
        verify(modelMapper).map(saved, UserResponse.class);
    }

    @Test
    @DisplayName("register: falla si email ya existe")
    void register_emailAlreadyExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(userCentral))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User already exists");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, modelMapper);
    }

    @Test
    @DisplayName("register: falla si username ya existe")
    void register_usernameAlreadyExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(userCentral))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User already exists");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, modelMapper);
    }

    // ---------- LOGIN ----------

    @Test
    @DisplayName("login: retorna UserResponse cuando credenciales son válidas (role como String)")
    void login_success() {
        User dbUser = cloneUser(userCentral);
        dbUser.setPassword("ENCODED");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(dbUser);
        when(passwordEncoder.matches("Secret123!", "ENCODED")).thenReturn(true);
        when(modelMapper.map(dbUser, UserResponse.class)).thenReturn(userResponseCentral);

        UserResponse resp = userService.login("alice@example.com", "Secret123!");

        assertThat(resp).isNotNull();
        assertThat(resp.getUsername()).isEqualTo("alice");
        assertThat(resp.getRole()).isEqualTo("CENTRAL");

        verify(userRepository).findByEmail("alice@example.com");
        verify(passwordEncoder).matches("Secret123!", "ENCODED");
        verify(modelMapper).map(dbUser, UserResponse.class);
    }

    @Test
    @DisplayName("login: lanza excepción si email no existe")
    void login_emailNotFound() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.login("nope@example.com", "pwd"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("login: lanza excepción si password no coincide")
    void login_badPassword() {
        User dbUser = cloneUser(userCentral);
        dbUser.setPassword("ENCODED");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(dbUser);
        when(passwordEncoder.matches("wrong", "ENCODED")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("alice@example.com", "wrong"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verifyNoInteractions(modelMapper);
    }

    // ---------- GET ALL ----------

    @Test
    @DisplayName("getAllUsers: mapea lista completa a UserResponse (id/role String)")
    void getAllUsers_mapsAll() {
        User u1 = cloneUser(userCentral);

        User u2 = new User();
        u2.setId(2L);
        u2.setUsername("bob");
        u2.setEmail("bob@example.com");
        u2.setPassword("ENC");
        u2.setRole(Role.BRANCH);
        u2.setBranch("LIMA-01");

        UserResponse r1 = new UserResponse();
        r1.setId("1");
        r1.setUsername("alice");
        r1.setEmail("alice@example.com");
        r1.setRole("CENTRAL");
        r1.setBranch(null);

        UserResponse r2 = new UserResponse();
        r2.setId("2");
        r2.setUsername("bob");
        r2.setEmail("bob@example.com");
        r2.setRole("BRANCH");
        r2.setBranch("LIMA-01");

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));
        when(modelMapper.map(u1, UserResponse.class)).thenReturn(r1);
        when(modelMapper.map(u2, UserResponse.class)).thenReturn(r2);

        List<UserResponse> list = userService.getAllUsers();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(UserResponse::getUsername)
                .containsExactly("alice", "bob");
        assertThat(list).extracting(UserResponse::getId)
                .containsExactly("1", "2");
        assertThat(list).extracting(UserResponse::getRole)
                .containsExactly("CENTRAL", "BRANCH");

        verify(userRepository).findAll();
        verify(modelMapper).map(u1, UserResponse.class);
        verify(modelMapper).map(u2, UserResponse.class);
    }

    // ---------- GET BY ID ----------

    @Test
    @DisplayName("getUserById: retorna mapeado cuando existe (id/role String)")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userCentral));
        when(modelMapper.map(userCentral, UserResponse.class)).thenReturn(userResponseCentral);

        UserResponse resp = userService.getUserById(1L);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo("1");
        assertThat(resp.getRole()).isEqualTo("CENTRAL");
        verify(userRepository).findById(1L);
        verify(modelMapper).map(userCentral, UserResponse.class);
    }

    @Test
    @DisplayName("getUserById: lanza excepción si no existe")
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        verify(modelMapper, never()).map(any(), eq(UserResponse.class));
    }

    // ---------- DELETE ----------

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deleteUser: elimina cuando existe")
        void deleteUser_success() {
            when(userRepository.existsById(10L)).thenReturn(true);

            userService.deleteUser(10L);

            verify(userRepository).existsById(10L);
            verify(userRepository).deleteById(10L);
        }

        @Test
        @DisplayName("deleteUser: lanza excepción cuando no existe")
        void deleteUser_notFound() {
            when(userRepository.existsById(10L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).existsById(10L);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }
}
