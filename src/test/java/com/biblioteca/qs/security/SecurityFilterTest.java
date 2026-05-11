package com.biblioteca.qs.security;

import com.biblioteca.qs.model.Usuario;
import com.biblioteca.qs.repository.UsuarioRepository;
import com.biblioteca.qs.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc // filtros habilitados
class SecurityFilterTest {

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
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("João");
        usuario.setEmail("user@email.com");
        usuario.setSenha(passwordEncoder.encode("senha123"));

        usuarioRepository.save(usuario);
    }

    @Test
    void deveContinuarCadeiaQuandoSemHeader() throws Exception {
        mockMvc.perform(get("/livros"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveContinuarCadeiaQuandoHeaderSemPrefixoBearer()
            throws Exception {

        mockMvc.perform(
                        get("/livros")
                                .header("Authorization", "token-sem-bearer")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveContinuarCadeiaQuandoBearerVazio() throws Exception {
        mockMvc.perform(
                        get("/livros")
                                .header("Authorization", "Bearer ")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar401QuandoTokenInvalido() throws Exception {
        mockMvc.perform(
                        get("/livros")
                                .header("Authorization", "Bearer token-invalido")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath(
                        "$.erro",
                        is("Token inválido ou expirado")
                ));
    }

    @Test
    void deveRetornar401QuandoTokenExpirado() throws Exception {
        String tokenExpirado = com.auth0.jwt.JWT.create()
                .withIssuer("biblioteca-api")
                .withSubject("user@email.com")
                .withExpiresAt(
                        java.time.Instant.now().minusSeconds(3600)
                )
                .sign(com.auth0.jwt.algorithms.Algorithm
                        .HMAC256("test-secret"));

        mockMvc.perform(
                        get("/livros")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenExpirado
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath(
                        "$.erro",
                        is("Token inválido ou expirado")
                ));
    }
    @Test
    void devePermitirAcessoComTokenValido() throws Exception {
        String token = tokenService.generateToken(usuario);

        mockMvc.perform(
                        get("/livros")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarAcessoQuandoUsuarioDeletadoAposToken()
            throws Exception {

        String token = tokenService.generateToken(usuario);

        usuarioRepository.deleteAll();

        mockMvc.perform(
                        get("/livros")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());
    }
}