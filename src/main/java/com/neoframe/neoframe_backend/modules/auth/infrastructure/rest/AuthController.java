package com.neoframe.neoframe_backend.modules.auth.infrastructure.rest;

import com.neoframe.neoframe_backend.modules.auth.core.ports.in.LoginUserUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.LoginWithGoogleUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.RegisterUserUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.AuthUseCase; // Mantido caso use para reset de senha
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LoginWithGoogleUseCase loginWithGoogleUseCase;
    private final AuthUseCase authUseCase; // Injetado para gerenciar o ciclo de vida de senhas

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          LoginWithGoogleUseCase loginWithGoogleUseCase,
                          AuthUseCase authUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.loginWithGoogleUseCase = loginWithGoogleUseCase;
        this.authUseCase = authUseCase;
    }

    /**
     * Cadastro por E-mail, Senha e Plano inicial (Padrão: STARTER se não enviado).
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        log.info("Starting registration process for email: {}", request.email());

        // Se o seu RegisterUserUseCase receber apenas email e password, passe eles.
        // Se receber o plano, use: request.plan()
        UUID userId = registerUserUseCase.execute(request.email(), request.password());

        RegisterResponse response = new RegisterResponse(
                userId,
                request.email(),
                request.plan() != null ? request.plan() : "STARTER",
                "Usuário registrado com sucesso!"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login tradicional por E-mail e Senha.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());
        String jwtToken = loginUserUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(jwtToken, "Login efetuado com sucesso."));
    }

    /**
     * Login / Cadastro Automático via Google OAuth2.
     */
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        log.info("Google OAuth login initialization attempt.");
        String jwtToken = loginWithGoogleUseCase.execute(request.idToken());
        return ResponseEntity.ok(new LoginResponse(jwtToken, "Autenticação via Google efetuada com sucesso."));
    }

    /**
     * Solicitação de recuperação de senha.
     * Retorna sempre 200 OK por boas práticas de segurança (evita enumeração de e-mails ativos).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.email());
        authUseCase.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    /**
     * Confirmação e redefinição da nova senha usando o token enviado por e-mail.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Processing password definition via token execution.");
        authUseCase.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Captura falhas de validação de credenciais ou regras do domínio de usuários
     * e impede estouros de erro 500 no frontend.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Authentication business rule violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}

// =========================================================================
// DTOs CONCENTRADOS (Evita que fiquem espalhados e quebrem o escopo do módulo)
// =========================================================================
record RegisterRequest(String email, String password, String plan) {}
record LoginRequest(String email, String password) {}
record GoogleLoginRequest(String idToken) {}
record ForgotPasswordRequest(String email) {}
record ResetPasswordRequest(String token, String newPassword) {}
record LoginResponse(String token, String message) {}
record RegisterResponse(UUID userId, String email, String plan, String message) {}