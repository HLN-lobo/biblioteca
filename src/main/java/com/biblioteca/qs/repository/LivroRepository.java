package com.biblioteca.qs.repository;

import com.biblioteca.qs.model.Livro;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface LivroRepository extends MongoRepository<Livro, String> {
    List<Livro> findByUsuarioId(String usuarioId); 
    List<Livro> findByUsuarioIdAndNomeContainingIgnoreCase(String usuarioId, String nome);
    List<Livro> findByUsuarioIdAndAutorContainingIgnoreCase(String usuarioId, String autor);
    List<Livro> findByUsuarioIdAndGeneroIgnoreCase(String usuarioId, String genero);
    List<Livro> findByUsuarioIdAndFavoritoTrue(String usuarioId);
}
