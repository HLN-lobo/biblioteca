package com.biblioteca.qs.controller;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.model.LivroDTO;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepository livroRepository;

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                mongoDBContainer::getReplicaSetUrl
        );

        registry.add(
                "api.security.token.secret",
                () -> "test-secret"
        );
    }

    @BeforeEach
    void setup(){

        livroRepository.deleteAll();

        livroRepository.save(
                new Livro(
                        "Clean Code",
                        "Boas práticas",
                        "url-capa"
                )
        );
    }
    @Test
    void deveListarLivros() throws Exception {

        mockMvc.perform(get("/livros/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$[0].nome",
                                is("Clean Code")
                        )
                );
    }
    @Test
    void deveBuscarLivroPorId() throws Exception {

        Livro livro = livroRepository.findAll()
                .getFirst();

        mockMvc.perform(
                        get("/livros/" + livro.getId())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.nome",
                                is("Clean Code")
                        )
                );
    }

    @Test
    void deveRetornar404AoBuscarLivroInexistente()
            throws Exception {

        mockMvc.perform(
                        get("/livros/id-invalido")
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void deveCriarLivro() throws Exception {

        String json = """
                {
                    "nome": "DDD",
                    "descricao": "Domain Driven Design",
                    "capa": "url-ddd"
                }
                """;

        mockMvc.perform(
                        post("/livros")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath(
                                "$.nome",
                                is("DDD")
                        )
                );
    }

    @Test
    void deveRetornar400AoCriarLivroSemNome()
            throws Exception {

        String json = """
                {
                    "nome": "",
                    "descricao": "Descrição",
                    "capa": "url"
                }
                """;

        mockMvc.perform(
                        post("/livros")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarLivro() throws Exception {

        Livro livro = livroRepository.findAll()
                .getFirst();

        String json = """
                {
                    "nome": "Livro Atualizado",
                    "descricao": "Nova descrição",
                    "capa": "nova-capa"
                }
                """;

        mockMvc.perform(
                        put("/livros/" + livro.getId())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.nome",
                                is("Livro Atualizado")
                        )
                );
    }

    @Test
    void deveRetornar404AoAtualizarLivroInexistente()
            throws Exception {

        String json = """
                {
                    "nome": "Livro",
                    "descricao": "Descrição",
                    "capa": "capa"
                }
                """;

        mockMvc.perform(
                        put("/livros/id-invalido")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDeletarLivro() throws Exception {

        Livro livro = livroRepository.findAll()
                .getFirst();

        mockMvc.perform(
                        delete("/livros/" + livro.getId())
                )
                .andExpect(status().isNoContent());

        assertThat(
                livroRepository.findById(livro.getId())
        ).isEmpty();
    }
}