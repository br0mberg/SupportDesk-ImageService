package ru.brombin.image_service.config;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.brombin.image_service.service.grpc.GrpcImageServiceImpl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                log.info("gRPC server shut down gracefully");
            } catch (InterruptedException e) {
                log.warn("gRPC server shutdown interrupted", e);
            }
        }));

        try {
            server.start();
            log.info("gRPC server started on port {}", grpcPort);
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при запуске gRPC сервера", e);
        }

        return server;
    }
}
