package com.neoframe.neoframe_backend.modules.auth.core.application;

import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan; // Certifique-se de que o Enum Plan existe aqui
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.RegisterUserUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;


    // Injeção de dependências via construtor
    public RegisterUserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(String email, String password) {
        log.info("Attempting to register a new user with email: {}", email);

        // 1. Validação de duplicidade
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed. Email '{}' is already taken.", email);
            throw new IllegalArgumentException("Este e-mail já está sendo utilizado por outra conta.");
        }

        // 2. Criptografia da senha (BCrypt gera um hash seguro e irreversível)
        // 3. Criação da entidade de usuário com o plano padrão de entrada (STARTER)
        User newUser = new User(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(password),
                Plan.STARTER, // Todo usuário novo começa no plano de entrada
                LocalDateTime.now()
        );

        // Retorna o objeto completo que o banco de dados salvou
        return userRepository.save(newUser);
    }
}
