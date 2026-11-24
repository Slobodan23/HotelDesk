package com.hoteldesk.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    private final SecretKey kljuc;

    public JwtConfig(@Value("${app.jwt.secret}") String secret) {
        byte[] bajtovi = secret.getBytes(StandardCharsets.UTF_8);
        if (bajtovi.length < 32) {
            throw new IllegalStateException("app.jwt.secret mora imati najmanje 32 karaktera");
        }
        this.kljuc = new SecretKeySpec(bajtovi, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(kljuc));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(kljuc).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
