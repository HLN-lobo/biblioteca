package com.biblioteca.qs.Livro;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest(properties = {
        "spring.session.store-type=none"
})
class LivroRepositoryTest {

    @Autowired
    private LivroRepository livroRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    private static final String USUARIO = "user1";

    @BeforeEach
    void setup() {
        livroRepository.deleteAll();

        Livro cleanCode = new Livro("Clean Code", "Robert Martin", "Tecnologia", "Boas práticas", "url1", USUARIO);
        Livro domQuixote = new Livro("Dom Quixote", "Cervantes", "Romance", "Clássico", "url2", USUARIO);
        Livro duna = new Livro("Duna", "Frank Herbert", "Ficção", "Espacial", "url3", "user2");

        cleanCode.setFavorito(true);

        livroRepository.saveAll(List.of(cleanCode, domQuixote, duna));
    }

    @Test
    void shouldReturnBooks() {
        List<Livro> livros = livroRepository.findAll();

        assertThat(livros).isNotEmpty();
        assertThat(livros.getFirst().getNome()).isEqualTo("Clean Code");
    }

    @Test
    void findByUsuarioId_retornaApenasLivrosDoUsuario() {
        List<Livro> resultado = livroRepository.findByUsuarioId(USUARIO);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(l -> l.getUsuarioId().equals(USUARIO));
    }

    @Test
    void findByNomeContaining_encontraPorSubstringCaseInsensitive() {
        List<Livro> resultado = livroRepository
                .findByUsuarioIdAndNomeContainingIgnoreCase(USUARIO, "clean");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getNome()).isEqualTo("Clean Code");
    }

    @Test
    void findByAutorContaining_encontraPorSubstringCaseInsensitive() {
        List<Livro> resultado = livroRepository
                .findByUsuarioIdAndAutorContainingIgnoreCase(USUARIO, "robert");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getAutor()).isEqualTo("Robert Martin");
    }

    @Test
    void findByGeneroIgnoreCase_filtraPorGeneroCaseInsensitive() {
        List<Livro> resultado = livroRepository
                .findByUsuarioIdAndGeneroIgnoreCase(USUARIO, "ROMANCE");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getNome()).isEqualTo("Dom Quixote");
    }

    @Test
    void findByFavoritoTrue_retornaApenasLivrosFavoritosDoUsuario() {
        List<Livro> resultado = livroRepository.findByUsuarioIdAndFavoritoTrue(USUARIO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getNome()).isEqualTo("Clean Code");
    }
}