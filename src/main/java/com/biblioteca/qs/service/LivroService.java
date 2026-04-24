package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService {
    @Autowired
    private LivroRepository livroRepository;
    public List<Livro> findAll() {
        return livroRepository.findAll();
    }
}
