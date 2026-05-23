package com.neoframe.neoframe_backend.shared.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.JwtTokenPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

        // 1. Extrai o token purificado do cabeçalho "Authorization"
        String token = extractToken(request);

        if (token != null) {
            try {
                // 2. Decodifica o token para inspecionar claims customizadas (Regra de proteção contra bypass)
                DecodedJWT decodedJWT = JWT.decode(token);
                String tokenType = decodedJWT.getClaim("type").asString();

                // Segurança máxima: Impede o uso de tokens de redefinição de senha nas rotas operacionais do SaaS
                if (tokenType == null || !tokenType.equals("PASSWORD_RESET")) {

                    // 3. Valida o token e extrai o ID/Subject do usuário através da porta da Arquitetura Hexagonal
                    // Nota: Se o seu método na JwtTokenPort retornar o e-mail, garanta que o token guarde o ID
                    // ou altere o nome da variável abaixo. O importante é passar o ID para bater com seus Controllers.
                    String userId = jwtTokenPort.validateTokenAndGetEmail(token);

                    // 4. Cria a autenticação injetando a String do ID diretamente como o "Principal"
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList() // Sem roles complexas por enquanto
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Autentica a requisição no contexto do Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // Token inválido, fraudado ou expirado. Limpamos o contexto por segurança.
                // O Spring Security cuidará de barrar o acesso nas rotas privadas mais adiante.
                SecurityContextHolder.clearContext();
            }
        }

        // Segue para o próximo filtro na cadeia do Spring
        filterChain.doFilter(request, response);
    }

    /**
     * Captura o cabeçalho "Authorization", valida o prefixo padrão e extrai o token JWT puro.
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}