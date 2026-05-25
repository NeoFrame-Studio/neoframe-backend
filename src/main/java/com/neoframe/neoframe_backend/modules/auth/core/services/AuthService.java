package com.neoframe.neoframe_backend.modules.auth.core.services;

import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan;
import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.AuthUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.EmailSenderPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.JwtTokenPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.PasswordEncoderPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenPort jwtTokenPort;
    private final EmailSenderPort emailSender;

    public AuthService(UserRepositoryPort userRepository,
                       PasswordEncoderPort passwordEncoder,
                       JwtTokenPort jwtTokenPort,
                       EmailSenderPort emailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenPort = jwtTokenPort;
        this.emailSender = emailSender;
    }

    @Override
    public User register(String email, String rawPassword, Plan plan) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Este e-mail já está em uso.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User(UUID.randomUUID(), email, encodedPassword, plan, LocalDateTime.now());

        return userRepository.save(newUser);
    }

    @Override
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        return jwtTokenPort.generateAuthToken(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        // Por segurança (evitar enumeração de usuários), não estouramos erro se o e-mail não existir.
        // Apenas ignoramos silenciosamente se não achar, ou enviamos o e-mail se achar.
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = jwtTokenPort.generatePasswordResetToken(user);
            emailSender.sendPasswordResetEmail(user.getEmail(), resetToken);
        });
    }

    @Override
    public void resetPassword(String token, String newRawPassword) {
        // O JwtTokenPort deve estourar exceção se o token for inválido ou estiver expirado
        String email = jwtTokenPort.validateTokenAndGetUserId(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para este token."));

        String encodedNewPassword = passwordEncoder.encode(newRawPassword);
        user.updatePassword(encodedNewPassword);

        userRepository.save(user);
    }
}
