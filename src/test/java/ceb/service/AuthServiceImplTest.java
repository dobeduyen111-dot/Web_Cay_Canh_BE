package ceb.service;

import java.util.Optional;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.exception.EmailAlreadyExistsException;
import ceb.exception.InvalidCredentialsException;
import ceb.exception.UserDisabledException;
import ceb.exception.UserNotFoundException;
import ceb.repository.UsersRepository;
import ceb.security.JwtService;
import ceb.security.TokenBlacklistService;
import ceb.service.implement.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;
    private TokenBlacklistService tokenBlacklistService;
    private JwtService jwtService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        usersRepository = mock(UsersRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        jwtService = mock(JwtService.class);
        authService = new AuthServiceImpl(usersRepository, passwordEncoder, tokenBlacklistService, jwtService);
    }

    @Test
    void registerNormalizesAndPersistsUser() {
        Users newUser = TestData.user();
        newUser.setUserId(0);
        newUser.setEmail(" a@example.com ");
        newUser.setPassword("secret");
        newUser.setPhone(" 090 000 0000 ");
        newUser.setRole(null);

        Users savedUser = TestData.user();
        savedUser.setPassword("encoded");
        when(usersRepository.findByEmail("a@example.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedUser));
        when(usersRepository.findByPhone("0900000000")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        Users result = authService.register(newUser);

        assertThat(result).isSameAs(savedUser);
        assertThat(newUser.getEmail()).isEqualTo("a@example.com");
        assertThat(newUser.getPhone()).isEqualTo("0900000000");
        assertThat(newUser.getRole()).isEqualTo("USER");
        assertThat(newUser.isEnabled()).isTrue();
        verify(usersRepository).save(newUser);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        Users user = TestData.user();
        when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.register(user))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void loginReturnsEnabledUserWhenPasswordMatches() {
        Users user = TestData.user();
        when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);

        assertThat(authService.login(user.getEmail(), "secret")).isSameAs(user);
    }

    @Test
    void loginRejectsDisabledOrInvalidCredentials() {
        Users disabled = TestData.user();
        disabled.setEnabled(false);
        when(usersRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(disabled));
        assertThatThrownBy(() -> authService.login("disabled@example.com", "secret"))
                .isInstanceOf(UserDisabledException.class);

        Users user = TestData.user();
        when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", user.getPassword())).thenReturn(false);
        assertThatThrownBy(() -> authService.login(user.getEmail(), "bad"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithGoogleCreatesMissingUser() {
        when(usersRepository.findByEmail("google@example.com"))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> {
                    Users user = TestData.user();
                    user.setEmail("google@example.com");
                    user.setFullName("Google User");
                    return Optional.of(user);
                });
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-random");

        Users result = authService.loginWithGoogle(" google@example.com ", " Google User ");

        assertThat(result.getEmail()).isEqualTo("google@example.com");
        verify(usersRepository).save(any(Users.class));
    }

    @Test
    void requestPasswordResetUpdatesEncodedPassword() {
        Users user = TestData.user();
        when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("new-secret", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new");

        authService.requestPasswordReset(user.getEmail(), "new-secret");

        verify(usersRepository).updatePassword(user.getUserId(), "encoded-new");
    }

    @Test
    void requestPasswordResetRequiresEnabledUser() {
        when(usersRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requestPasswordReset("missing@example.com", "new-secret"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void logoutBlacklistsTokenUntilJwtExpiration() {
        when(jwtService.getTokenExpiration("token")).thenReturn(12345L);

        authService.logout("token");

        verify(tokenBlacklistService).blacklistToken("token", 12345L);
    }
}
