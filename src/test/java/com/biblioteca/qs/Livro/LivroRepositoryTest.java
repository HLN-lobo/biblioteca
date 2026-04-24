package com.biblioteca.qs.Livro;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
@DataMongoTest(properties = {
        "spring.session.store-type=none"
})
public class LivroRepositoryTest {
    @Autowired
    private LivroRepository livroRepository;
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    @BeforeEach
    void setup() {
        livroRepository.deleteAll();

        livroRepository.save(new Livro(
                "Clean Code",
                "Boas práticas",
                "url-capa"
        ));
    }

    @Test
    void shouldReturnBooks() throws Exception{
        List<Livro> livro = livroRepository.findAll();
        assertThat(livro).isNotEmpty();
        assertThat(livro.getFirst().getNome()).isEqualTo("Clean Code");
        }
}
