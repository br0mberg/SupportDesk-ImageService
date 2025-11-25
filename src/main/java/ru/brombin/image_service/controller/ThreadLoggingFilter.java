package ru.brombin.image_service.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class ThreadLoggingFilter extends OncePerRequestFilter {

    /* Фильтр нужен, исключительно, для визуализации работы потоков до/после включения loom */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        Thread currentThread = Thread.currentThread();

        log.info("HTTP {} {} -> thread name='{}', virtual='{}'",
                request.getMethod(),
                request.getRequestURI(),
                currentThread.getName(),
                currentThread.isVirtual());

        filterChain.doFilter(request, response);
    }
}
