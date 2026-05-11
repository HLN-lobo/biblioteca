package com.biblioteca.qs.controller;

import com.biblioteca.qs.model.LivroDTO;
import com.biblioteca.qs.model.Livro;
import com.biblioteca.qs.service.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroService livroService;

    @GetMapping("/todos")
    public ResponseEntity<List<Livro>> getAllLivros() {
        return ResponseEntity.ok(livroService.findAll());
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listar(
            @RequestParam String usuarioId,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String ordem) {
        return ResponseEntity.ok(livroService.buscarEFiltrar(usuarioId, busca, genero, ordem));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscar(@PathVariable String id) {
        return ResponseEntity.ok(livroService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Livro> criar(@RequestBody LivroDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livroService.salvar(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizar(@PathVariable String id, @RequestBody LivroDTO body) {
        return ResponseEntity.ok(livroService.atualizar(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        livroService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/avaliacao")
    public ResponseEntity<Livro> avaliar(@PathVariable String id,
                                          @RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(livroService.avaliar(id, body.get("avaliacao")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Card 10 — PATCH favorito (toggle)
    @PatchMapping("/{id}/favorito")
    public ResponseEntity<Livro> favoritar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(livroService.alternarFavorito(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}