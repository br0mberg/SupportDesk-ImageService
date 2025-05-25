package ru.brombin.image_service.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationService {
    @NonFinal
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuerUri;

    JwtAuthenticationConverter jwtAuthenticationConverter;

    public void authenticateJwt(String jwtToken) {
        try {
            log.debug("Attempting to authenticate JWT token...");
            Jwt jwt = decodeJwt(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationConverter.convert(jwt));
            log.info("JWT token successfully authenticated.");
        } catch (JwtException e) {
            log.error("JWT authentication failed", e);
            throw new SecurityException("Невалидный или истекший JWT токен.", e);
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
            throw new SecurityException("Ошибка в процессе аутентификации.", e);
        }
    }

    private Jwt decodeJwt(String jwtToken) {
        try {
            log.debug("Decoding JWT token...");
            JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
            Jwt jwt = jwtDecoder.decode(jwtToken);
            log.debug("JWT token decoded successfully.");
            return jwt;
        } catch (JwtException e) {
            log.error("Error decoding JWT token", e);
            throw new SecurityException("Ошибка декодирования JWT токена.", e);
        }
    }
}
