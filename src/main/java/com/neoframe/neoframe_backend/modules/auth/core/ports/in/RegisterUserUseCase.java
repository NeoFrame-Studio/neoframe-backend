package com.neoframe.neoframe_backend.modules.auth.core.ports.in;

import com.neoframe.neoframe_backend.modules.auth.core.domain.User;

public interface RegisterUserUseCase {
    // Mudou de UUID para User
    User execute(String email, String password);
}