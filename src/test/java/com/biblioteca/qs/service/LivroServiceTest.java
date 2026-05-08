package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @CsvSource({
        "Clean Code, Robert Martin, Tecnologia, Boas práticas, url1, user1",
        "Dom Quixote, Cervantes, Romance, Classico, url2, user1",
        "Duna, Frank Herbert, Ficção, Espacial, url3, user2"
    })
    @Order(1)
    void deveSalvarLivroComDadosVariados(String nome, String autor, String genero,
                                          String descricao, String capa, String usuarioId) {
        Livro livro = new Livro(nome, autor, genero, descricao, capa, usuarioId);
        Livro salvo = livroService.salvar(livro);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo(nome);
        assertThat(salvo.getAutor()).isEqualTo(autor);
        assertThat(salvo.isFavorito()).isFalse();
        assertThat(salvo.getAvaliacao()).isNull();
    }

    @Test
    @Order(2)
    void deveListarSomenteLivrosDoUsuario() {
        livroService.salvar(new Livro("Livro A", "Autor A", "Genero", "Desc", "capa", "user1"));
        livroService.salvar(new Livro("Livro B", "Autor B", "Genero", "Desc", "capa", "user1"));
        livroService.salvar(new Livro("Livro C", "Autor C", "Genero", "Desc", "capa", "user2"));

        List<Livro> livrosUser1 = livroService.findByUsuario("user1");
        assertThat(livrosUser1).hasSize(2);
        assertThat(livrosUser1).allMatch(l -> l.getUsuarioId().equals("user1"));
    }

    @Test
    @Order(3)
    void deveAtualizarLivro() {
        Livro salvo = livroService.salvar(new Livro("Nome Antigo", "Autor", "Genero", "Desc", "capa", "user1"));

        Livro atualizado = new Livro("Nome Novo", "Autor Novo", "Genero Novo", "Desc Nova", "capa2", "user1");
        Livro resultado = livroService.atualizar(salvo.getId(), atualizado);

        assertThat(resultado.getNome()).isEqualTo("Nome Novo");
        assertThat(resultado.getAutor()).isEqualTo("Autor Novo");
    }

    @Test
    @Order(4)
    void deveLancarExcecaoAoAtualizarLivroInexistente() {
        Livro livroFalso = new Livro("X", "Y", "Z", "W", "url", "user1");
        assertThatThrownBy(() -> livroService.atualizar("id-invalido", livroFalso))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @Order(5)
    void deveDeletarLivroExistente() {
        Livro salvo = livroService.salvar(new Livro("Para deletar", "Autor", "Genero", "Desc", "capa", "user1"));
        livroService.deletar(salvo.getId());

        assertThat(livroService.buscarPorId(salvo.getId())).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "Clean, user1, 1",
        "Martin, user1, 1",
        "Duna, user1, 0"
    })
    @Order(6)
    void deveBuscarPorNomeOuAutor(String busca, String usuarioId, int esperado) {
        livroService.salvar(new Livro("Clean Code", "Robert Martin", "Tech", "Desc", "url", "user1"));

        List<Livro> resultado = livroService.buscarEFiltrar(usuarioId, busca, null, null);
        assertThat(resultado).hasSize(esperado);
    }

    @Test
    @Order(7)
    void deveFiltrarPorGenero() {
        livroService.salvar(new Livro("Livro 1", "Autor", "Romance", "Desc", "url", "user1"));
        livroService.salvar(new Livro("Livro 2", "Autor", "Tecnologia", "Desc", "url", "user1"));

        List<Livro> resultado = livroService.buscarEFiltrar("user1", null, "Romance", null);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getGenero()).isEqualTo("Romance");
    }

    @Test
    @Order(8)
    void deveOrdenarAZ() {
        livroService.salvar(new Livro("Zebra", "Autor", "Gen", "Desc", "url", "user1"));
        livroService.salvar(new Livro("Abacaxi", "Autor", "Gen", "Desc", "url", "user1"));
        livroService.salvar(new Livro("Manga", "Autor", "Gen", "Desc", "url", "user1"));

        List<Livro> resultado = livroService.buscarEFiltrar("user1", null, null, "az");
        assertThat(resultado.get(0).getNome()).isEqualTo("Abacaxi");
        assertThat(resultado.get(2).getNome()).isEqualTo("Zebra");
    }

    @Test
    @Order(9)
    void deveOrdenarZA() {
        livroService.salvar(new Livro("Zebra", "Autor", "Gen", "Desc", "url", "user1"));
        livroService.salvar(new Livro("Abacaxi", "Autor", "Gen", "Desc", "url", "user1"));

        List<Livro> resultado = livroService.buscarEFiltrar("user1", null, null, "za");
        assertThat(resultado.getFirst().getNome()).isEqualTo("Zebra");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @Order(10)
    void deveAvaliarLivroComValoresValidos(int avaliacao) {
        Livro salvo = livroService.salvar(new Livro("Livro", "Autor", "Gen", "Desc", "url", "user1"));
        Livro avaliado = livroService.avaliar(salvo.getId(), avaliacao);

        assertThat(avaliado.getAvaliacao()).isEqualTo(avaliacao);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    @Order(11)
    void deveRejeitarAvaliacaoForaDoIntervalo(int avaliacaoInvalida) {
        Livro salvo = livroService.salvar(new Livro("Livro", "Autor", "Gen", "Desc", "url", "user1"));

        assertThatThrownBy(() -> livroService.avaliar(salvo.getId(), avaliacaoInvalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1 e 5");
    }

    @Test
    @Order(12)
    void deveLancarExcecaoAoAvaliarLivroInexistente() {
        assertThatThrownBy(() -> livroService.avaliar("id-invalido", 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @Order(13)
    void deveAlternarFavoritoParaTrue() {
        Livro salvo = livroService.salvar(new Livro("Livro", "Autor", "Gen", "Desc", "url", "user1"));
        assertThat(salvo.isFavorito()).isFalse();

        Livro favoritado = livroService.alternarFavorito(salvo.getId());
        assertThat(favoritado.isFavorito()).isTrue();
    }

    @Test
    @Order(14)
    void deveAlternarFavoritoParaFalse() {
        Livro salvo = livroService.salvar(new Livro("Livro", "Autor", "Gen", "Desc", "url", "user1"));
        livroService.alternarFavorito(salvo.getId()); // true
        Livro desfavoritado = livroService.alternarFavorito(salvo.getId()); // false

        assertThat(desfavoritado.isFavorito()).isFalse();
    }

    @Test
    @Order(15)
    void deveLancarExcecaoAoFavoritarLivroInexistente() {
        assertThatThrownBy(() -> livroService.alternarFavorito("id-invalido"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }
}