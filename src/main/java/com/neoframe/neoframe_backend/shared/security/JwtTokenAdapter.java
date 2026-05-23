package com.neoframe.neoframe_backend.shared.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.JwtTokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtTokenAdapter implements JwtTokenPort {

    private final String secretKey;
    private final String issuer;

    // Injeta propriedades do application.properties, mas provê defaults para não quebrar
    public JwtTokenAdapter(
            @Value("${jwt.secret:defaultSecretKeyNeoFrameVerySecure2026!}") String secretKey,
            @Value("${jwt.issuer:neoframe-api}") String issuer) {
        this.secretKey = secretKey;
        this.issuer = issuer;
    }

    @Override
    public String generateAuthToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getEmail()) // O subject é sempre o e-mail
                .withClaim("userId", user.getId().toString())
                .withClaim("plan", user.getPlan().name())
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    @Override
    public String generatePasswordResetToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        // Token de recuperação dura apenas 15 minutos
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getEmail())
                .withClaim("type", "PASSWORD_RESET")
                .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .sign(algorithm);
    }

    @Override
    public String validateTokenAndGetEmail(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject(); // Retorna o e-mail de dentro do token
        } catch (JWTVerificationException exception) {
            throw new IllegalArgumentException("Token inválido ou expirado.");
        }
    }
}