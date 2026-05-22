package com.neoframe.neoframe_backend.core.ports.in;

import java.util.UUID;

public interface RegisterUserUseCase {
    /**
     * Registra um novo usuário no sistema com a senha criptografada e plano inicial.
     *
     * @return O ID único do usuário recém-criado
     */
    UUID execute(String email, String rawPassword);
}