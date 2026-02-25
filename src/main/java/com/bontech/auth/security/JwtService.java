package com.bontech.auth.security;

import com.bontech.auth.entity.UserAccount;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long expirationSeconds;

    public JwtService(JwtEncoder jwtEncoder,
                      JwtDecoder jwtDecoder,
                      @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.expirationSeconds = expirationSeconds;
    }

    public String generate(UserAccount user, String actor) {
        Instant now = Instant.now();
        Set<String> roles = user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.getUsername())
                .claim("roles", roles)
                .claim("tenant_id", String.valueOf(user.getTenant().getId()))
                .claim("tenant_name", user.getTenant().getName())
                .claim("username", user.getUsername())
                .claim("actor", actor == null ? user.getUsername() : actor)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(() -> "HS256").build(), claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String extractTokenId(String token) {
        return jwtDecoder.decode(token).getId();
    }
}
