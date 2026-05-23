package com.neoframe.neoframe_backend.modules.auth.core.ports.in;

public interface LoginWithGoogleUseCase {
    /**
     * Valida o token recebido do Google, cria o usuário se não existir,
     * e retorna o token JWT nativo do NeoFrame.
     */
    String execute(String idTokenString);
}