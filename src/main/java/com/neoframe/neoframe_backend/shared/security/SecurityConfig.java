package com.neoframe.neoframe_backend.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // Puxa do application.properties as URLs permitidas (Vite local, domínio final no Railway, etc.)
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Aplica as regras de CORS para permitir conversas com o Frontend React
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Desativa CSRF porque a API é Stateless e usa tokens JWT salvos no LocalStorage
                .csrf(csrf -> csrf.disable())

                // 3. Define que nossa API não guardará estado de sessão no servidor
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Mapeamento cirúrgico de acessos públicos e privados
                .authorizeHttpRequests(auth -> auth
                        // Libera endpoints de autenticação (Cadastro, Login e Google OAuth2)
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Libera o Callback do Python (a segurança real é validada via token X-Internal-Key direto no Controller)
                        .requestMatchers("/api/v1/videos/internal/**").permitAll()

                        // Qualquer outra rota do ecossistema NeoFrame exige o token JWT válido
                        .anyRequest().authenticated()
                )

                // 5. Injeta o filtro customizado de extração do JWT antes do validador padrão do Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Converte a string do properties separada por vírgulas em uma lista de origens reais para o CORS
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Adicionado o 'X-Internal-Key' para permitir que o script do Python faça requisições cross-origin se necessário
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Internal-Key"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Define o BCrypt robusto para fazer o hash seguro das senhas antes de salvar no banco
        return new BCryptPasswordEncoder();
    }
}