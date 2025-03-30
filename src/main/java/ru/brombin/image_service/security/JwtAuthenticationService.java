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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationService {
    @NonFinal
    @Value("${spring.security.oauth2.resource.server.jwt.issuer-uri}")
    String issuerUri;

    JwtAuthenticationConverter jwtAuthenticationConverter;

    public void authenticateJwt(String jwtToken) {
        try {
            Jwt jwt = decodeJwt(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationConverter.convert(jwt));
        } catch (Exception e) {
            throw new SecurityException("Невалидный или истекший JWT токен.", e);
        }
    }

    private Jwt decodeJwt(String jwtToken) {
        try {
            JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

            return jwtDecoder.decode(jwtToken);
        } catch (JwtException e) {
            throw new SecurityException("Ошибка декодирования JWT токена.", e);
        }
    }
}
