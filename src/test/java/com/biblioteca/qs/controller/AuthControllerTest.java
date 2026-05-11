package com.biblioteca.qs.controller;

import com.biblioteca.qs.model.Usuario;
import com.biblioteca.qs.repository.UsuarioRepository;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }
    @Test
    void deveRegistrarUsuarioComSucesso() throws Exception {
        String json = """
                {
                    "nome": "João",
                    "email": "joao@email.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("João")))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void deveRetornar409AoRegistrarEmailJaExistente() throws Exception {
        Usuario existente = new Usuario();
        existente.setNome("João");
        existente.setEmail("joao@email.com");
        existente.setSenha(passwordEncoder.encode("senha123"));
        usuarioRepository.save(existente);

        String json = """
                {
                    "nome": "João",
                    "email": "joao@email.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Usuário já existe")));
    }

    @Test
    void deveRetornar400AoRegistrarSemNome() throws Exception {
        String json = """
                {
                    "nome": "",
                    "email": "joao@email.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("O nome é obrigatório")));
    }

    @Test
    void deveRetornar400AoRegistrarSemEmail() throws Exception {
        String json = """
                {
                    "nome": "João",
                    "email": "",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("O email é obrigatório")));
    }

    @Test
    void deveRetornar400AoRegistrarSemSenha() throws Exception {
        String json = """
                {
                    "nome": "João",
                    "email": "joao@email.com",
                    "senha": ""
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("A senha é obrigatória")));
    }

    @Test
    void deveRetornar400AoRegistrarComSenhaCurta() throws Exception {
        String json = """
                {
                    "nome": "João",
                    "email": "joao@email.com",
                    "senha": "123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.error",
                        is("A senha deve ter no mínimo 6 caracteres")
                ));
    }
    @Test
    void deveRealizarLoginComSucesso() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("João");
        usuario.setEmail("joao@email.com");
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuarioRepository.save(usuario);

        String json = """
                {
                    "email": "joao@email.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("João")))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void deveRetornar404AoLoginComEmailInexistente() throws Exception {
        String json = """
                {
                    "email": "naoexiste@email.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(
                        "$.error",
                        is("Usuário não encontrado")
                ));
    }

    @Test
    void deveRetornar401AoLoginComSenhaErrada() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("João");
        usuario.setEmail("joao@email.com");
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuarioRepository.save(usuario);

        String json = """
                {
                    "email": "joao@email.com",
                    "senha": "senha-errada"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Senha inválida")));
    }
}