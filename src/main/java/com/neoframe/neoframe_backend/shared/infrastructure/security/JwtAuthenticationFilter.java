package com.neoframe.neoframe_backend.shared.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.neoframe.neoframe_backend.core.ports.out.JwtTokenPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenPort jwtTokenPort;

    public JwtAuthenticationFilter(JwtTokenPort jwtTokenPort) {
        this.jwtTokenPort = jwtTokenPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                // Se o token for inválido, vai estourar a IllegalArgumentException do nosso adapter
                String email = jwtTokenPort.validateTokenAndGetEmail(token);

                // Ignorar tokens do tipo "PASSWORD_RESET" para requisições de API normais
                DecodedJWT decodedJWT = JWT.decode(token);
                String tokenType = decodedJWT.getClaim("type").asString();

                if (tokenType == null || !tokenType.equals("PASSWORD_RESET")) {
                    setAuthenticationInContext(email);
                }

            } catch (Exception e) {
                // Token inválido, expirado ou falha na validação. Apenas ignoramos.
                // O Spring Security vai barrar a requisição adiante se a rota for protegida.
                SecurityContextHolder.clearContext();
            }
        }

        // Passa a bola para o próximo filtro na cadeia
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setAuthenticationInContext(String email) {
        // Criamos um UserDetails simples só com o e-mail, sem roles complexas no momento
        UserDetails userDetails = User.withUsername(email)
                .password("") // Senha não importa aqui
                .authorities(Collections.emptyList())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}