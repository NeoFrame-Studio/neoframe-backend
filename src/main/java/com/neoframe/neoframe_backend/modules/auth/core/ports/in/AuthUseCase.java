package com.neoframe.neoframe_backend.modules.auth.core.ports.in;

import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan;
import com.neoframe.neoframe_backend.modules.auth.core.domain.User;

public interface AuthUseCase {
    User register(String email, String rawPassword, Plan plan);
    String login(String email, String rawPassword);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newRawPassword);
}