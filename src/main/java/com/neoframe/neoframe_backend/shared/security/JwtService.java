package com.neoframe.neoframe_backend.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // Na versão 0.12.5, usamos SecretKey do pacote javax.crypto
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Gera o token atualizado para os métodos da v0.12.5
    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString()) // Mudou de setSubject para subject
                .claim("email", email)
                .issuedAt(now)              // Mudou de setIssuedAt para issuedAt
                .expiration(expiryDate)     // Mudou de setExpiration para expiration
                .signWith(getSigningKey())  // O algoritmo HS256 agora é detectado automaticamente pela chave
                .compact();
    }

    // Extrai o ID do usuário usando o novo parser() e verifyWith()
    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // Mudou de setSigningKey para verifyWith
                .build()
                .parseSignedClaims(token)    // Mudou de parseClaimsJws para parseSignedClaims
                .getPayload();               // Mudou de getBody para getPayload
        return claims.getSubject();
    }

    // Valida o token no padrão novo
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}