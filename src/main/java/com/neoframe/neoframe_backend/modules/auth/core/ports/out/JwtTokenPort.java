package com.neoframe.neoframe_backend.modules.auth.core.ports.out;

import com.neoframe.neoframe_backend.modules.auth.core.domain.User;

public interface JwtTokenPort {
    String generateAuthToken(User user);
    String generatePasswordResetToken(User user);
    String validateTokenAndGetEmail(String token); // Retorna o e-mail se o token for válido
}
