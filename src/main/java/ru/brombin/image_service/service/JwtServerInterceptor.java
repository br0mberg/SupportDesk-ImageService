package ru.brombin.image_service.service;

import io.grpc.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.brombin.image_service.security.JwtAuthenticationService;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtServerInterceptor implements ServerInterceptor {
    JwtAuthenticationService jwtAuthenticationService;

    static Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        String jwtToken = headers.get(AUTHORIZATION_KEY);
        if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("JWT токен отсутствует или некорректен."), headers);
            return new ServerCall.Listener<>() {};
        }

        jwtToken = jwtToken.substring("Bearer ".length());

        try {
            jwtAuthenticationService.authenticateJwt(jwtToken);
        } catch (SecurityException e) {
            call.close(Status.UNAUTHENTICATED.withDescription(e.getMessage()), headers);
            return new ServerCall.Listener<>() {};
        }

        return next.startCall(call, headers);
    }
}
