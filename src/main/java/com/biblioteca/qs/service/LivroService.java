package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.model.LivroDTO;
import com.biblioteca.qs.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;

    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    public Livro buscarPorId(String id) {

        return livroRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Livro não encontrado"
                        )
                );
    }

    public Livro salvar(LivroDTO body) {

        validarLivro(body);

        Livro livro = new Livro();

        livro.setNome(body.nome());
        livro.setDescricao(body.descricao());
        livro.setCapa(body.capa());

        return livroRepository.save(livro);
    }

    public Livro atualizar(String id, LivroDTO body) {

        validarLivro(body);

        Livro livro = buscarPorId(id);

        livro.setNome(body.nome());
        livro.setDescricao(body.descricao());
        livro.setCapa(body.capa());

        return livroRepository.save(livro);
    }

    public void deletar(String id) {

        Livro livro = buscarPorId(id);

        livroRepository.delete(livro);
    }

    private void validarLivro(LivroDTO body){

        if(body.nome() == null || body.nome().isBlank()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do livro é obrigatório"
            );
        }

        if(body.descricao() == null || body.descricao().isBlank()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A descrição do livro é obrigatória"
            );
        }
    }
}