package com.neoframe.neoframe_backend.modules.auth.core.application;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan;
import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.ports.in.LoginWithGoogleUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.JwtTokenPort;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class LoginWithGoogleService implements LoginWithGoogleUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginWithGoogleService.class);

    private final UserRepositoryPort userRepository;
    private final JwtTokenPort jwtTokenPort; // Substituiu o JwtService
    private final PasswordEncoder passwordEncoder;
    private final String googleClientId;

    public LoginWithGoogleService(UserRepositoryPort userRepository,
                                  JwtTokenPort jwtTokenPort,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.google.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.jwtTokenPort = jwtTokenPort;
        this.passwordEncoder = passwordEncoder;
        this.googleClientId = googleClientId;
    }

    @Override
    public String execute(String idTokenString) {
        log.info("Processing Google login token validation.");

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Google authentication failed. Token is invalid or forged.");
                throw new IllegalArgumentException("Token do Google inválido ou expirado.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            log.info("Google identity verified successfully for email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("First time login via Google for user '{}'. Auto-creating account.", email);
                        User newUser = new User(
                                UUID.randomUUID(),
                                email,
                                passwordEncoder.encode(UUID.randomUUID().toString()),
                                Plan.STARTER,
                                LocalDateTime.now()
                        );
                        return userRepository.save(newUser);
                    });

            // Devolve o JWT oficial do NeoFrame usando a Porta
            return jwtTokenPort.generateAuthToken(user);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Critical error during Google Token verification: ", e);
            throw new IllegalArgumentException("Falha interna ao autenticar com o Google.");
        }
    }
}