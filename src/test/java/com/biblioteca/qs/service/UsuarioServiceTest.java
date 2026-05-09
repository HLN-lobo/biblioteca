package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Usuario;
import com.biblioteca.qs.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UsuarioServiceTest {

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
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {
        Usuario usuario = usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123"
        );

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getEmail()).isEqualTo("user@email.com");
        assertThat(usuario.getNome()).isEqualTo("João");
    }

    @Test
    void deveSalvarSenhaEncoded() {
        Usuario usuario = usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123"
        );

        assertThat(usuario.getSenha())
                .isNotEqualTo("senha123");
    }

    @Test
    void devePersistirUsuarioNoBanco() {
        usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123"
        );

        assertThat(
                usuarioRepository.findByEmail("user@email.com")
        ).isPresent();
    }

    @Test
    void deveLancarExcecaoAoCadastrarEmailJaExistente() {
        usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123"
        );

        assertThatThrownBy(() ->
                usuarioService.cadastrar(
                        "user@email.com",
                        "Maria",
                        "outrasenha"
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("user@email.com");
    }

    @Test
    void naoDeveCadastrarSegundoUsuarioComMesmoEmail() {
        usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123"
        );

        assertThatThrownBy(() ->
                usuarioService.cadastrar(
                        "user@email.com",
                        "Maria",
                        "outrasenha"
                )
        );

        assertThat(usuarioRepository.findAll())
                .hasSize(1);
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123"
        );

        Usuario encontrado =
                usuarioService.buscarPorEmail("user@email.com");

        assertThat(encontrado.getEmail())
                .isEqualTo("user@email.com");
    }

    @Test
    void deveLancarExcecaoAoBuscarEmailInexistente() {
        assertThatThrownBy(() ->
                usuarioService.buscarPorEmail("naoexiste@email.com")
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("naoexiste@email.com");
    }
}