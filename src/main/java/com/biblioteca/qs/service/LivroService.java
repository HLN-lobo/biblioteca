package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.model.LivroDTO;
import com.biblioteca.qs.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;

    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    public List<Livro> findByUsuario(String usuarioId) {
        return livroRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Livro> buscarPorId(String id) {
        return livroRepository.findById(id);
    }

    public Livro salvar(LivroDTO body) {
        validarLivro(body);

        Livro livro = new Livro();
        livro.setNome(body.nome());
        livro.setAutor(body.autor());
        livro.setGenero(body.genero());
        livro.setDescricao(body.descricao());
        livro.setCapa(body.capa());
        livro.setUsuarioId(body.usuarioId());

        return livroRepository.save(livro);
    }

    public Livro atualizar(String id, LivroDTO dto) {
        return livroRepository.findById(id).map(livro -> {
            livro.setNome(dto.nome());
            livro.setAutor(dto.autor());
            livro.setGenero(dto.genero());
            livro.setDescricao(dto.descricao());
            livro.setCapa(dto.capa());
            return livroRepository.save(livro);
        }).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Livro não encontrado: " + id));
    }
    public void deletar(String id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livro não encontrado: " + id));

        livroRepository.delete(livro);
    }

    private void validarLivro(LivroDTO body) {
        if (body.nome() == null || body.nome().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do livro é obrigatório"
            );
        }

        if (body.descricao() == null || body.descricao().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A descrição do livro é obrigatória"
            );
        }
    }

    public List<Livro> buscarEFiltrar(String usuarioId, String busca, String genero, String ordem) {
        List<Livro> livros = new ArrayList<>();

        if (busca != null && !busca.isBlank()) {
            List<Livro> porNome = livroRepository
                    .findByUsuarioIdAndNomeContainingIgnoreCase(usuarioId, busca);
            List<Livro> porAutor = livroRepository
                    .findByUsuarioIdAndAutorContainingIgnoreCase(usuarioId, busca);

            livros.addAll(porNome);
            porAutor.forEach(l -> {
                if (livros.stream().noneMatch(x -> x.getId().equals(l.getId()))) {
                    livros.add(l);
                }
            });
        } else {
            livros.addAll(livroRepository.findByUsuarioId(usuarioId));
        }

        if (genero != null && !genero.isBlank()) {
            livros.removeIf(l -> !genero.equalsIgnoreCase(l.getGenero()));
        }

        if ("az".equalsIgnoreCase(ordem)) {
            livros.sort(Comparator.comparing(Livro::getNome, String.CASE_INSENSITIVE_ORDER));
        } else if ("za".equalsIgnoreCase(ordem)) {
            livros.sort(Comparator.comparing(Livro::getNome, String.CASE_INSENSITIVE_ORDER).reversed());
        }

        return livros;
    }

    public Livro avaliar(String id, Integer avaliacao) {
        if (avaliacao < 1 || avaliacao > 5) {
            throw new IllegalArgumentException("Avaliação deve ser entre 1 e 5");
        }
        return livroRepository.findById(id).map(livro -> {
            livro.setAvaliacao(avaliacao);
            return livroRepository.save(livro);
        }).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Livro não encontrado: " + id));
    }

    public Livro alternarFavorito(String id) {
        return livroRepository.findById(id).map(livro -> {
            livro.setFavorito(!livro.isFavorito());
            return livroRepository.save(livro);
        }).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Livro não encontrado: " + id));
    }
}