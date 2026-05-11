package com.biblioteca.qs.security;

import com.biblioteca.qs.repository.UsuarioRepository;
import com.biblioteca.qs.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String login = tokenService.validateToken(token);

        if (login == null || login.isBlank()) {
            sendErrorResponse(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Token inválido ou expirado"
            );
            return;
        }

        usuarioRepository.findByEmail(login)
                .ifPresentOrElse(
                        user -> {
                            var authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user,
                                            null,
                                            Collections.emptyList()
                                    );
                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);
                        },
                        () -> {
                            SecurityContextHolder.clearContext();
                        }
                );

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        String token = header.substring(7).strip();

        return token.isBlank() ? null : token;
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "status", status.value(),
                        "erro", message
                )
        );
    }
}