package com.hoteldesk.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registracija").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hoteli/moji").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/hoteli",
                                "/api/hoteli/{id}",
                                "/api/hoteli/{id}/sobe",
                                "/api/hoteli/{id}/sobe/slobodne",
                                "/api/hoteli/{id}/recenzije",
                                "/api/sobe/{id}",
                                "/api/drzave/**",
                                "/api/gradovi/**",
                                "/api/tipovi-soba/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((req, res, e) ->
                                posaljiGresku(res, HttpServletResponse.SC_UNAUTHORIZED, "Korisnik nije prijavljen ili je token istekao"))
                        .accessDeniedHandler((req, res, e) ->
                                posaljiGresku(res, HttpServletResponse.SC_FORBIDDEN, "Nemate pravo pristupa")))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                posaljiGresku(res, HttpServletResponse.SC_UNAUTHORIZED, "Korisnik nije prijavljen"))
                        .accessDeniedHandler((req, res, e) ->
                                posaljiGresku(res, HttpServletResponse.SC_FORBIDDEN, "Nemate pravo pristupa")));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter uloge = new JwtGrantedAuthoritiesConverter();
        uloge.setAuthoritiesClaimName(JwtService.CLAIM_ULOGA);
        uloge.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(uloge);
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}") List<String> origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void posaljiGresku(HttpServletResponse response, int status, String poruka) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + status
                + ",\"message\":\"" + poruka + "\",\"errors\":{}}");
    }
}
