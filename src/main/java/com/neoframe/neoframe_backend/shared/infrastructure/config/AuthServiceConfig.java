package com.neoframe.neoframe_backend.shared.infrastructure.config;

import com.neoframe.neoframe_backend.core.ports.in.AuthUseCase;
import com.neoframe.neoframe_backend.core.ports.out.EmailSenderPort;
import com.neoframe.neoframe_backend.core.ports.out.JwtTokenPort;
import com.neoframe.neoframe_backend.core.ports.out.PasswordEncoderPort;
import com.neoframe.neoframe_backend.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.core.services.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthServiceConfig {

    @Bean
    public AuthUseCase authUseCase(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            JwtTokenPort jwtTokenPort,
            EmailSenderPort emailSender) {

        // Instancia o serviço puro do domínio injetando as implementações da infraestrutura
        return new AuthService(userRepository, passwordEncoder, jwtTokenPort, emailSender);
    }
}