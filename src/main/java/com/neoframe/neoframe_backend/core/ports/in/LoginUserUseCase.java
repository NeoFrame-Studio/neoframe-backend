package com.neoframe.neoframe_backend.core.ports.in;

public interface LoginUserUseCase {
    /**
     * Autentica um usuário pelas credenciais e gera o Token JWT de acesso.
     *
     * @return O token JWT gerado para a sessão do usuário
     */
    String execute(String email, String rawPassword);
}
