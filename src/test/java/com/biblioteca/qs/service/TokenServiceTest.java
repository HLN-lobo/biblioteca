package com.biblioteca.qs.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.biblioteca.qs.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class TokenServiceTest {

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                mongoDBContainer::getReplicaSetUrl
        );
    }

    @Autowired
    private TokenService tokenService;
    @Value("${api.security.token.secret}")
    private String secret;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setEmail("user@email.com");
    }

    @Test
    void deveGerarTokenValido() {
        String token = tokenService.generateToken(usuario);

        assertThat(token).isNotBlank();
    }

    @Test
    void tokenDeveConterEmailComoSubject() {
        String token = tokenService.generateToken(usuario);

        String subject = JWT
                .require(Algorithm.HMAC256(secret))
                .withIssuer("biblioteca-api")
                .build()
                .verify(token)
                .getSubject();

        assertThat(subject).isEqualTo("user@email.com");
    }

    @Test
    void tokenDeveConterIssuerCorreto() {
        String token = tokenService.generateToken(usuario);

        String issuer = JWT
                .require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getIssuer();

        assertThat(issuer).isEqualTo("biblioteca-api");
    }

    @Test
    void tokenDeveExpirarEm24Horas() {
        String token = tokenService.generateToken(usuario);

        Instant expiration = JWT
                .require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getExpiresAtAsInstant();

        assertThat(expiration)
                .isAfter(Instant.now().plusSeconds(23 * 3600))
                .isBefore(Instant.now().plusSeconds(25 * 3600));
    }

    @Test
    void deveValidarTokenERetornarEmail() {
        String token = tokenService.generateToken(usuario);

        String email = tokenService.validateToken(token);

        assertThat(email).isEqualTo("user@email.com");
    }

    @Test
    void deveRetornarVazioParaTokenInvalido() {
        String email = tokenService.validateToken("token.invalido.aqui");

        assertThat(email).isEmpty();
    }

    @Test
    void deveRetornarVazioParaTokenComAssinaturaErrada() {
        String tokenOutroSecret = JWT.create()
                .withIssuer("biblioteca-api")
                .withSubject("user@email.com")
                .sign(Algorithm.HMAC256("outro-secret"));

        String email = tokenService.validateToken(tokenOutroSecret);

        assertThat(email).isEmpty();
    }

    @Test
    void deveRetornarVazioParaTokenExpirado() {
        String tokenExpirado = JWT.create()
                .withIssuer("biblioteca-api")
                .withSubject("user@email.com")
                .withExpiresAt(Instant.now().minusSeconds(3600))
                .sign(Algorithm.HMAC256("test-secret"));

        String email = tokenService.validateToken(tokenExpirado);

        assertThat(email).isEmpty();
    }

    @Test
    void deveRetornarVazioParaTokenComIssuerErrado() {
        String tokenIssuerErrado = JWT.create()
                .withIssuer("outro-issuer")
                .withSubject("user@email.com")
                .sign(Algorithm.HMAC256("test-secret"));

        String email = tokenService.validateToken(tokenIssuerErrado);

        assertThat(email).isEmpty();
    }

    @Test
    void deveRetornarVazioParaTokenNulo() {
        String email = tokenService.validateToken(null);

        assertThat(email).isEmpty();
    }

    @Test
    void deveRetornarVazioParaTokenVazio() {
        String email = tokenService.validateToken("");

        assertThat(email).isEmpty();
    }
}