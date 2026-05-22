package com.neoframe.neoframe_backend.core.ports.out;

import com.neoframe.neoframe_backend.core.domain.User;

public interface JwtTokenPort {
    String generateAuthToken(User user);
    String generatePasswordResetToken(User user);
    String validateTokenAndGetEmail(String token); // Retorna o e-mail se o token for válido
}
