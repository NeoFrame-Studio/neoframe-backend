package com.neoframe.neoframe_backend.shared.infrastructure.web.dto;

import com.neoframe.neoframe_backend.core.domain.Plan;

// Usamos uma classe envelope para agrupar os Records e organizar o projeto
public class AuthDtos {

    public record RegisterRequest(String email, String password, Plan plan) {}

    public record LoginRequest(String email, String password) {}

    public record ForgotPasswordRequest(String email) {}

    public record ResetPasswordRequest(String token, String newPassword) {}

    public record TokenResponse(String token) {}

    public record UserResponse(String id, String email, String plan) {}
}