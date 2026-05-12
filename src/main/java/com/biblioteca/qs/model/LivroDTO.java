package com.biblioteca.qs.model;

public record LivroDTO(
        String nome,
        String descricao,
        String capa,
        String autor,
        String genero,
        String usuarioId
) {}