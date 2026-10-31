package com.brightloop.security;

import com.brightloop.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(@Value("${security.jwt.secret-base64}") String secret,
                      @Value("${security.jwt.expiration-seconds:900}") long expirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationSeconds = expirationSeconds;
    }

    public IssuedToken issue(User user) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(expirationSeconds);
        String tokenId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(tokenId)
                .subject(user.getEmail())
                .claim("uid", user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, tokenId, expires);
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public record IssuedToken(String value, String id, Instant expiresAt) { }
}
