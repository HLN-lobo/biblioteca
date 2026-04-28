package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LivroService {
    private final LivroRepository livroRepository;

    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    public Optional<Livro> buscarPorId(String id) {
        return livroRepository.findById(id);
    }
        public Livro salvar(Livro livro) {
        return livroRepository.save(livro);
    }

    public Livro atualizar(String id, Livro livroAtualizado) {
        return livroRepository.findById(id).map(livro -> {
            livro.setNome(livroAtualizado.getNome());
            livro.setDescricao(livroAtualizado.getDescricao());
            livro.setCapa(livroAtualizado.getCapa());
            return livroRepository.save(livro);
        }).orElseThrow(() -> new RuntimeException("Livro não encontrado: " + id));
    }

    public void deletar(String id) {
        livroRepository.deleteById(id);
    }
}
