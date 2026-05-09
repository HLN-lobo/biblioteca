package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.model.LivroDTO;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LivroServiceTest {

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
    private LivroService livroService;

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void limparBanco() {
        livroRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "Clean Code, Livro sobre boas práticas, url1",
            "DDD, Livro sobre Domain Driven Design, url2",
            "Refactoring, Livro sobre refatoração, url3"
    })
    @Order(1)
    void deveSalvarLivroComSucesso(
            String nome,
            String descricao,
            String capa
    ) {

        LivroDTO dto = new LivroDTO(
                nome,
                descricao,
                capa
        );

        Livro livroSalvo = livroService.salvar(dto);

        assertThat(livroSalvo.getId()).isNotNull();

        assertThat(livroSalvo.getNome())
                .isEqualTo(nome);

        assertThat(livroSalvo.getDescricao())
                .isEqualTo(descricao);

        assertThat(livroSalvo.getCapa())
                .isEqualTo(capa);
    }

    @Test
    @Order(2)
    void deveLancarErroAoSalvarLivroSemNome() {

        LivroDTO dto = new LivroDTO(
                "",
                "Descrição",
                "capa"
        );

        assertThatThrownBy(() ->
                livroService.salvar(dto)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nome do livro é obrigatório");
    }

    @Test
    @Order(3)
    void deveLancarErroAoSalvarLivroSemDescricao() {

        LivroDTO dto = new LivroDTO(
                "Livro",
                "",
                "capa"
        );

        assertThatThrownBy(() ->
                livroService.salvar(dto)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("descrição do livro é obrigatória");
    }
    @Test
    @Order(4)
    void deveListarTodosLivros() {

        livroService.salvar(
                new LivroDTO(
                        "Livro A",
                        "Descrição A",
                        "capa-a"
                )
        );

        livroService.salvar(
                new LivroDTO(
                        "Livro B",
                        "Descrição B",
                        "capa-b"
                )
        );

        List<Livro> livros = livroService.findAll();

        assertThat(livros)
                .hasSize(2);
    }
    @Test
    @Order(5)
    void deveBuscarLivroPorId() {

        Livro livroSalvo = livroService.salvar(
                new LivroDTO(
                        "Livro",
                        "Descrição",
                        "capa"
                )
        );

        Livro livroEncontrado =
                livroService.buscarPorId(livroSalvo.getId());

        assertThat(livroEncontrado)
                .isNotNull();

        assertThat(livroEncontrado.getId())
                .isEqualTo(livroSalvo.getId());
    }

    @Test
    @Order(6)
    void deveLancarErroAoBuscarLivroInexistente() {

        assertThatThrownBy(() ->
                livroService.buscarPorId("id-invalido")
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }
    @Test
    @Order(7)
    void deveAtualizarLivro() {

        Livro livroSalvo = livroService.salvar(
                new LivroDTO(
                        "Livro Antigo",
                        "Descrição antiga",
                        "capa-antiga"
                )
        );

        LivroDTO dtoAtualizado = new LivroDTO(
                "Livro Novo",
                "Nova descrição",
                "nova-capa"
        );

        Livro livroAtualizado = livroService.atualizar(
                livroSalvo.getId(),
                dtoAtualizado
        );

        assertThat(livroAtualizado.getNome())
                .isEqualTo("Livro Novo");

        assertThat(livroAtualizado.getDescricao())
                .isEqualTo("Nova descrição");

        assertThat(livroAtualizado.getCapa())
                .isEqualTo("nova-capa");
    }

    @Test
    @Order(8)
    void deveLancarErroAoAtualizarLivroInexistente() {

        LivroDTO dto = new LivroDTO(
                "Livro",
                "Descrição",
                "capa"
        );

        assertThatThrownBy(() ->
                livroService.atualizar("id-invalido", dto)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @Test
    @Order(9)
    void deveDeletarLivro() {

        Livro livroSalvo = livroService.salvar(
                new LivroDTO(
                        "Livro",
                        "Descrição",
                        "capa"
                )
        );

        livroService.deletar(livroSalvo.getId());

        assertThatThrownBy(() ->
                livroService.buscarPorId(livroSalvo.getId())
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @Test
    @Order(10)
    void deveLancarErroAoDeletarLivroInexistente() {

        assertThatThrownBy(() ->
                livroService.deletar("id-invalido")
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }
}