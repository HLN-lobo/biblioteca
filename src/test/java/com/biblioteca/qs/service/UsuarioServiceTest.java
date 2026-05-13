package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Usuario;
import com.biblioteca.qs.repository.UsuarioRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UsuarioServiceTest {

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    static WireMockServer wireMock;

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.mongodb.uri",
                mongoDBContainer::getReplicaSetUrl
        );

        registry.add(
                "viacep.base-url",
                () -> "http://localhost:" + wireMock.port()
        );
    }

    @BeforeAll
    static void iniciarWireMock() {

        wireMock = new WireMockServer(
                WireMockConfiguration.options().dynamicPort()
        );

        wireMock.start();
    }

    @AfterAll
    static void encerrarWireMock() {

        wireMock.stop();
    }

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limparBanco() {

        usuarioRepository.deleteAll();

        wireMock.resetAll();
    }

    private void gravarCassete(String cep, String body) {

        wireMock.stubFor(
                get(urlEqualTo("/ws/" + cep + "/json/"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(body)
                        )
        );
    }

    // ==========================
    // TESTES ORIGINAIS
    // ==========================

    @Test
    void deveCadastrarUsuarioComSucesso() {

        Usuario usuario = usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123",
                null
        );

        assertThat(usuario.getId()).isNotNull();

        assertThat(usuario.getEmail())
                .isEqualTo("user@email.com");

        assertThat(usuario.getNome())
                .isEqualTo("João");
    }

    @Test
    void deveSalvarSenhaEncoded() {

        Usuario usuario = usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123",
                null
        );

        assertThat(usuario.getSenha())
                .isNotEqualTo("senha123");
    }

    @Test
    void devePersistirUsuarioNoBanco() {

        usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123",
                null
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
                "senha123",
                null
        );

        assertThatThrownBy(() ->
                usuarioService.cadastrar(
                        "user@email.com",
                        "Maria",
                        "outrasenha",
                        null
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
                "senha123",
                null
        );

        assertThatThrownBy(() ->
                usuarioService.cadastrar(
                        "user@email.com",
                        "Maria",
                        "outrasenha",
                        null
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
                "senha123",
                null
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

    @Test
    void deveCadastrarUsuarioComCepValido() {

        gravarCassete(
                "01310100",
                """
                {
                  "cep": "01310100",
                  "logradouro": "Avenida Paulista",
                  "bairro": "Bela Vista",
                  "localidade": "São Paulo",
                  "uf": "SP"
                }
                """
        );

        Usuario usuario = usuarioService.cadastrar(
                "user@email.com",
                "João",
                "senha123",
                "01310100"
        );

        assertThat(usuario.getEndereco()).isNotNull();

        assertThat(
                usuario.getEndereco().getLocalidade()
        ).isEqualTo("São Paulo");

        assertThat(
                usuario.getEndereco().getUf()
        ).isEqualTo("SP");

        assertThat(
                usuario.getEndereco().getLogradouro()
        ).isEqualTo("Avenida Paulista");
    }

    @Test
    void deveLancarExcecaoComCepInvalido() {

        assertThatThrownBy(() ->
                usuarioService.cadastrar(
                        "user@email.com",
                        "João",
                        "senha123",
                        "123"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP inválido");
    }

    @Test
    void deveLancarExcecaoComCepInexistente() {

        gravarCassete(
                "00000000",
                """
                {
                  "erro": true
                }
                """
        );

        assertThatThrownBy(() ->
                usuarioService.cadastrar(
                        "user@email.com",
                        "João",
                        "senha123",
                        "00000000"
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }
}