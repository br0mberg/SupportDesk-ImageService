package ru.brombin.image_service.config;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.brombin.image_service.service.grpc.GrpcImageServiceImpl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GrpcServerConfig {

    @NonFinal
    @Value("${grpc.server.port}")
    Integer grpcPort;

    @NonFinal
    @Value("${grpc.server.max.inbound.message.size.bytes}")
    Integer maxInboundMessageSize;

    GrpcImageServiceImpl grpcImageServiceImpl;

    @Bean
    public Server grpcServer() {
        Server server = NettyServerBuilder.forPort(grpcPort)
                .maxInboundMessageSize(maxInboundMessageSize)
                .permitKeepAliveTime(5, TimeUnit.MINUTES)
                .addService(grpcImageServiceImpl)
                .build();

        try {
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при запуске gRPC сервера", e);
        }

        return server;
    }
}