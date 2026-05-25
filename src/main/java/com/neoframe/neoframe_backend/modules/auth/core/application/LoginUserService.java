package com.neoframe.neoframe_backend.modules.auth.core.application;

import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.LoginUserUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.JwtTokenPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUserService implements LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenPort jwtTokenPort; // Substituiu o JwtService

    public LoginUserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder, JwtTokenPort jwtTokenPort) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenPort = jwtTokenPort;
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

        // 3. Credenciais válidas: Gera e retorna o Token JWT do NeoFrame usando a Porta
        String token = jwtTokenPort.generateAuthToken(user); // Código muito mais limpo!
        log.info("User [{}] successfully authenticated. JWT token issued.", user.getId());

        return token;
    }
}