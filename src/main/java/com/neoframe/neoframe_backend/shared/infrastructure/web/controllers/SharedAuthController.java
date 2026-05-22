package com.neoframe.neoframe_backend.shared.infrastructure.web.controllers;

import com.neoframe.neoframe_backend.core.domain.User;
import com.neoframe.neoframe_backend.core.ports.in.AuthUseCase;
import com.neoframe.neoframe_backend.shared.infrastructure.web.dto.AuthDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class SharedAuthController {

    private final AuthUseCase authUseCase;

    public SharedAuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User newUser = authUseCase.register(request.email(), request.password(), request.plan());
        UserResponse response = new UserResponse(
                newUser.getId().toString(),
                newUser.getEmail(),
                newUser.getPlan().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        String token = authUseCase.login(request.email(), request.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authUseCase.requestPasswordReset(request.email());
        // Sempre retorna 200 OK para evitar falhas de segurança (enumeração de e-mails)
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authUseCase.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    // Intercepta as exceções de regra de negócio do Core e devolve 400 em vez de 500
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}