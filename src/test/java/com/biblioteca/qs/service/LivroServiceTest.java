package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.model.LivroDTO;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
        registry.add(
                "api.security.token.secret",
                () -> "test-secret"
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
    void deveSalvarLivroComSucesso(String nome, String descricao, String capa) {
        LivroDTO dto = new LivroDTO(nome, descricao, capa, null, null, null);

        Livro salvo = livroService.salvar(dto);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo(nome);
        assertThat(salvo.getDescricao()).isEqualTo(descricao);
        assertThat(salvo.getCapa()).isEqualTo(capa);
        assertThat(salvo.isFavorito()).isFalse();
        assertThat(salvo.getAvaliacao()).isNull();
    }

    @Test
    @Order(2)
    void deveLancarErroAoSalvarLivroSemNome() {
        LivroDTO dto = new LivroDTO("", "Descrição", "capa", null, null, null);

        assertThatThrownBy(() -> livroService.salvar(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nome do livro é obrigatório");
    }

    @Test
    @Order(3)
    void deveLancarErroAoSalvarLivroSemDescricao() {
        LivroDTO dto = new LivroDTO("Livro", "", "capa", null, null, null);

        assertThatThrownBy(() -> livroService.salvar(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("descrição do livro é obrigatória");
    }

    @Test
    @Order(4)
    void deveListarTodosLivros() {
        livroService.salvar(new LivroDTO("Livro A", "Descrição A", "capa-a", null, null, null));
        livroService.salvar(new LivroDTO("Livro B", "Descrição B", "capa-b", null, null, null));

        assertThat(livroService.findAll()).hasSize(2);
    }

    @Test
    @Order(5)
    void deveListarSomenteLivrosDoUsuario() {
        livroService.salvar(new LivroDTO("Livro A", "Desc", "url", null, null, "user1"));
        livroService.salvar(new LivroDTO("Livro B", "Desc", "url", null, null, "user1"));
        livroService.salvar(new LivroDTO("Livro C", "Desc", "url", null, null, "user2"));

        List<Livro> livrosUser1 = livroService.findByUsuario("user1");

        assertThat(livrosUser1).hasSize(2);
        assertThat(livrosUser1).allMatch(l -> l.getUsuarioId().equals("user1"));
    }

    @Test
    @Order(6)
    void deveBuscarLivroPorId() {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro", "Descrição", "capa", null, null, null)
        );

        Livro encontrado = livroService.buscarPorId(salvo.getId())
                .orElseThrow();

        assertThat(encontrado.getId()).isEqualTo(salvo.getId());
    }

    @Test
    @Order(7)
    void deveLancarErroAoBuscarLivroInexistente() {
        assertThatThrownBy(() -> livroService.buscarPorId("id-invalido")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livro não encontrado")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @Test
    @Order(8)
    void deveAtualizarLivro() {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro Antigo", "Desc antiga", "capa-antiga", null, null, null)
        );

        Livro atualizado = livroService.atualizar(
                salvo.getId(),
                new LivroDTO("Livro Novo", "Nova descrição", "nova-capa", null, null, null)
        );

        assertThat(atualizado.getNome()).isEqualTo("Livro Novo");
        assertThat(atualizado.getDescricao()).isEqualTo("Nova descrição");
        assertThat(atualizado.getCapa()).isEqualTo("nova-capa");
    }

    @Test
    @Order(9)
    void deveLancarErroAoAtualizarLivroInexistente() {
        LivroDTO dto = new LivroDTO("Livro", "Descrição", "capa", null, null, null);

        assertThatThrownBy(() -> livroService.atualizar("id-invalido", dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @Test
    @Order(10)
    void deveDeletarLivro() {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro", "Descrição", "capa", null, null, null)
        );

        livroService.deletar(salvo.getId());

        assertThat(livroService.buscarPorId(salvo.getId())).isEmpty();
    }

    @Test
    @Order(11)
    void deveLancarErroAoDeletarLivroInexistente() {
        assertThatThrownBy(() -> livroService.deletar("id-invalido"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @ParameterizedTest
    @CsvSource({
            "Clean, user1, 1",
            "Martin, user1, 1",
            "Duna,   user1, 0"
    })
    @Order(12)
    void deveBuscarPorNomeOuAutor(String busca, String usuarioId, int esperado) {
        livroService.salvar(
                new LivroDTO("Clean Code", "Desc", "url", "Robert Martin", null, "user1")
        );

        List<Livro> resultado = livroService.buscarEFiltrar(usuarioId, busca, null, null);

        assertThat(resultado).hasSize(esperado);
    }

    @Test
    @Order(13)
    void deveFiltrarPorGenero() {
        livroService.salvar(new LivroDTO("Livro 1", "Desc", "url", null, null, "user1"));
        livroService.salvar(new LivroDTO("Livro 2", "Desc", "url", null, null, "user1"));

        Livro l1 = livroService.findByUsuario("user1").get(0);
        livroService.atualizar(
                l1.getId(),
                new LivroDTO(l1.getNome(), l1.getDescricao(), l1.getCapa(), null, null, "user1")
        );

        List<Livro> resultado = livroService.buscarEFiltrar("user1", null, "Romance", null);

        assertThat(resultado).isEmpty();
    }

    @Test
    @Order(14)
    void deveOrdenarAZ() {
        livroService.salvar(new LivroDTO("Zebra",   "Desc", "url", null, null, "user1"));
        livroService.salvar(new LivroDTO("Abacaxi", "Desc", "url", null, null, "user1"));
        livroService.salvar(new LivroDTO("Manga",   "Desc", "url", null, null, "user1"));

        List<Livro> resultado = livroService.buscarEFiltrar("user1", null, null, "az");

        assertThat(resultado.get(0).getNome()).isEqualTo("Abacaxi");
        assertThat(resultado.get(2).getNome()).isEqualTo("Zebra");
    }

    @Test
    @Order(15)
    void deveOrdenarZA() {
        livroService.salvar(new LivroDTO("Zebra",   "Desc", "url", null, null, "user1"));
        livroService.salvar(new LivroDTO("Abacaxi", "Desc", "url", null, null, "user1"));

        List<Livro> resultado = livroService.buscarEFiltrar("user1", null, null, "za");

        assertThat(resultado.getFirst().getNome()).isEqualTo("Zebra");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @Order(16)
    void deveAvaliarLivroComValoresValidos(int avaliacao) {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro", "Desc", "url", null, null, null)
        );

        Livro avaliado = livroService.avaliar(salvo.getId(), avaliacao);

        assertThat(avaliado.getAvaliacao()).isEqualTo(avaliacao);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    @Order(17)
    void deveRejeitarAvaliacaoForaDoIntervalo(int avaliacaoInvalida) {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro", "Desc", "url", null, null, null)
        );

        assertThatThrownBy(() -> livroService.avaliar(salvo.getId(), avaliacaoInvalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1 e 5");
    }

    @Test
    @Order(18)
    void deveLancarExcecaoAoAvaliarLivroInexistente() {
        assertThatThrownBy(() -> livroService.avaliar("id-invalido", 3))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @Order(19)
    void deveAlternarFavoritoParaTrue() {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro", "Desc", "url", null, null, null)
        );
        assertThat(salvo.isFavorito()).isFalse();

        Livro favoritado = livroService.alternarFavorito(salvo.getId());

        assertThat(favoritado.isFavorito()).isTrue();
    }

    @Test
    @Order(20)
    void deveAlternarFavoritoParaFalse() {
        Livro salvo = livroService.salvar(
                new LivroDTO("Livro", "Desc", "url", null, null, null)
        );
        livroService.alternarFavorito(salvo.getId());

        Livro desfavoritado = livroService.alternarFavorito(salvo.getId());

        assertThat(desfavoritado.isFavorito()).isFalse();
    }

    @Test
    @Order(21)
    void deveLancarExcecaoAoFavoritarLivroInexistente() {
        assertThatThrownBy(() -> livroService.alternarFavorito("id-invalido"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não encontrado");
    }
}