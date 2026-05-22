package com.neoframe.neoframe_backend.shared.security;

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

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Pega o cabeçalho de autorização da requisição HTTP
        String authHeader = request.getHeader("Authorization");

        // Se não tiver token ou não começar com "Bearer ", passa para o próximo filtro (Spring vai barrar se a rota for protegida)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extrai apenas o código do token (removendo a palavra "Bearer ")
        String token = authHeader.substring(7);

        // 3. Valida a assinatura e expiração do token
        if (jwtService.isTokenValid(token)) {
            String userId = jwtService.extractUserId(token);

            // Cria o objeto de autenticação injetando o ID do usuário como o "Principal" do Spring
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.emptyList() // Sem roles/permissões complexas por enquanto (SaaS simples)
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Grava a autenticação no contexto atual da requisição
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Segue o fluxo normal da requisição
        filterChain.doFilter(request, response);
    }
}