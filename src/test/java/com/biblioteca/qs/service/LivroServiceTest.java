package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private LivroService livroService;

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void limpar() {
        livroRepository.deleteAll();
    }

    // --- Testes parametrizados (caixa preta) ---

    @ParameterizedTest
    @CsvSource({
        "Clean Code, Robert Martin, url1",
        "Domain-Driven Design, Eric Evans, url2",
        "Refactoring, Martin Fowler, url3"
    })
    @Order(1)
    void deveSalvarLivroComDadosVariados(String nome, String descricao, String capa) {
        Livro livro = new Livro(nome, descricao, capa);
        Livro salvo = livroService.salvar(livro);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo(nome);
    }

    @Test
    @Order(2)
    void deveListarTodosOsLivros() {
        livroService.salvar(new Livro("Livro A", "Desc A", "capa-a"));
        livroService.salvar(new Livro("Livro B", "Desc B", "capa-b"));

        List<Livro> livros = livroService.findAll();
        assertThat(livros).hasSize(2);
    }

    @Test
    @Order(3)
    void deveLancarExcecaoAoAtualizarLivroInexistente() {
        Livro livroFalso = new Livro("X", "Y", "Z");
        assertThatThrownBy(() -> livroService.atualizar("id-invalido", livroFalso))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @Order(4)
    void deveDeletarLivroExistente() {
        Livro salvo = livroService.salvar(new Livro("Para deletar", "Desc", "capa"));
        livroService.deletar(salvo.getId());

        assertThat(livroService.buscarPorId(salvo.getId())).isEmpty();
    }
}