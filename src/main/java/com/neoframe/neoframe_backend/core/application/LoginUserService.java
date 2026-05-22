package com.neoframe.neoframe_backend.core.application;

import com.neoframe.neoframe_backend.core.domain.User;
import com.neoframe.neoframe_backend.core.ports.in.LoginUserUseCase;
import com.neoframe.neoframe_backend.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.shared.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUserService implements LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginUserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String execute(String email, String rawPassword) {
        log.info("Processing login attempt for email: {}", email);

        // 1. Busca o usuário pelo e-mail
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed. Email '{}' not found.", email);
                    return new IllegalArgumentException("E-mail ou senha incorretos.");
                });

        // 2. Valida se a senha bate com o Hash do banco de dados
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Login failed for user [{}]. Incorrect password.", user.getId());
            throw new IllegalArgumentException("E-mail ou senha incorretos.");
        }

        // 3. Credenciais válidas: Gera e retorna o Token JWT do NeoFrame
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("User [{}] successfully authenticated. JWT token issued.", user.getId());

        return token;
    }
}
