package com.brightloop.security;

import com.brightloop.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokens;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwtService, RevokedTokenRepository revokedTokens,
                                  UserRepository users) {
        this.jwtService = jwtService;
        this.revokedTokens = revokedTokens;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                String tokenId = claims.getId();
                if (tokenId == null || revokedTokens.existsById(tokenId)) {
                    unauthorized(response);
                    return;
                }
                var user = users.findById(java.util.UUID.fromString(claims.get("uid", String.class)))
                        .filter(account -> account.isEnabled() && account.getEmail().equals(claims.getSubject()))
                        .orElse(null);
                if (user == null) {
                    unauthorized(response);
                    return;
                }
                var authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name())).toList();
                var authentication = new UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                unauthorized(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or expired access token\"}");
    }
}
