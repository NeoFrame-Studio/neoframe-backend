package com.neoframe.neoframe_backend.modules.auth.infrastructure.rest;

import com.neoframe.neoframe_backend.core.ports.in.LoginUserUseCase;
import com.neoframe.neoframe_backend.core.ports.in.LoginWithGoogleUseCase;
import com.neoframe.neoframe_backend.core.ports.in.RegisterUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class ModuleAuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LoginWithGoogleUseCase loginWithGoogleUseCase;

    public ModuleAuthController(RegisterUserUseCase registerUserUseCase,
                                LoginUserUseCase loginUserUseCase,
                                LoginWithGoogleUseCase loginWithGoogleUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.loginWithGoogleUseCase = loginWithGoogleUseCase;
    }

    // Cadastro por E-mail e Senha
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody AuthRequest request) {
        UUID userId = registerUserUseCase.execute(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(userId, "Usuário registrado com sucesso!"));
    }

    // Login por E-mail e Senha
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest request) {
        String jwtToken = loginUserUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(jwtToken, "Login efetuado com sucesso."));
    }

    // Login / Cadastro Automático via Google OAuth2
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        String jwtToken = loginWithGoogleUseCase.execute(request.idToken());
        return ResponseEntity.ok(new LoginResponse(jwtToken, "Autenticação via Google efetuada com sucesso."));
    }
}

// DTOs de mapeamento das requisições
record AuthRequest(String email, String password) {}
record GoogleLoginRequest(String idToken) {}
record RegisterResponse(UUID userId, String message) {}
record LoginResponse(String token, String message) {}