package com.neoframe.neoframe_backend.modules.auth.core.services;

import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan;
import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.EmailSenderPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.JwtTokenPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.PasswordEncoderPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private JwtTokenPort jwtTokenPort;

    @Mock
    private EmailSenderPort emailSender;

    @InjectMocks
    private AuthService authService;

    private User validUser;
    private final String EMAIL = "user@neoframe.com";
    private final String RAW_PASSWORD = "secret_password_123";
    private final String ENCODED_PASSWORD = "secret_password_hash";

    @BeforeEach
    void setUp() {
        // Base user object to be used across tests
        validUser = new User(
                UUID.randomUUID(),
                EMAIL,
                ENCODED_PASSWORD,
                mock(Plan.class),
                LocalDateTime.now()
        );
    }

    // ==========================================
    // TESTS FOR METHOD: register
    // ==========================================

    @Test
    @DisplayName("Should successfully register a new user")
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = authService.register(EMAIL, RAW_PASSWORD, mock(Plan.class));

        // Assert
        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());

        // Verify if the password was encoded before saving
        verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw an exception when trying to register an already existing email")
    void register_ThrowsException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(EMAIL, RAW_PASSWORD, mock(Plan.class));
        });

        assertEquals("Este e-mail já está em uso.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensures save was not called
    }

    // ==========================================
    // TESTS FOR METHOD: login
    // ==========================================

    @Test
    @DisplayName("Should login and return a token with correct credentials")
    void login_Success() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenPort.generateAuthToken(validUser)).thenReturn("valid_jwt_token");

        // Act
        String token = authService.login(EMAIL, RAW_PASSWORD);

        // Assert
        assertEquals("valid_jwt_token", token);
    }

    @Test
    @DisplayName("Should throw an exception when logging in with a non-existent email")
    void login_ThrowsException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(EMAIL, RAW_PASSWORD);
        });

        assertEquals("Credenciais inválidas.", exception.getMessage());
        verify(jwtTokenPort, never()).generateAuthToken(any()); // Token should not be generated
    }

    @Test
    @DisplayName("Should throw an exception when logging in with an incorrect password")
    void login_ThrowsException_WhenPasswordIsWrong() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(EMAIL, RAW_PASSWORD);
        });

        assertEquals("Credenciais inválidas.", exception.getMessage());
        verify(jwtTokenPort, never()).generateAuthToken(any());
    }

    // ==========================================
    // TESTS FOR METHOD: requestPasswordReset
    // ==========================================

    @Test
    @DisplayName("Should send a recovery email if the user exists")
    void requestPasswordReset_SendsEmail_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(validUser));
        when(jwtTokenPort.generatePasswordResetToken(validUser)).thenReturn("reset_token_123");

        // Act
        authService.requestPasswordReset(EMAIL);

        // Assert
        verify(emailSender, times(1)).sendPasswordResetEmail(EMAIL, "reset_token_123");
    }

    @Test
    @DisplayName("Should fail silently (do nothing) if the user does not exist")
    void requestPasswordReset_DoesNothing_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act
        authService.requestPasswordReset(EMAIL);

        // Assert
        verify(jwtTokenPort, never()).generatePasswordResetToken(any());
        verify(emailSender, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // ==========================================
    // TESTS FOR METHOD: resetPassword
    // ==========================================

    @Test
    @DisplayName("Should successfully reset the password given a valid token")
    void resetPassword_Success() {
        // Arrange
        String resetToken = "valid_reset_token";
        String newRawPassword = "new_password_321";
        String newEncodedPassword = "new_password_hash";

        when(jwtTokenPort.validateTokenAndGetUserId(resetToken)).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode(newRawPassword)).thenReturn(newEncodedPassword);

        // Act
        authService.resetPassword(resetToken, newRawPassword);

        // Assert
        verify(passwordEncoder, times(1)).encode(newRawPassword);

        // Verify if the repository called save with the updated user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
    }

    @Test
    @DisplayName("Should throw an exception if the token returns an email that does not exist in the database")
    void resetPassword_ThrowsException_WhenUserNotFound() {
        // Arrange
        String resetToken = "valid_reset_token";

        when(jwtTokenPort.validateTokenAndGetUserId(resetToken)).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword(resetToken, "new_password_321");
        });

        assertEquals("Usuário não encontrado para este token.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}