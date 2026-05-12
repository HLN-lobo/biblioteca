package com.biblioteca.qs.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "livros")
public class Livro {

    @Id
    private String id;

    private String nome;
    private String descricao;
    private String capa;

    private String autor;
    private String genero;
    private Integer avaliacao;
    private boolean favorito;

    private String usuarioId;

    public Livro() {
    }

    public Livro(String nome, String descricao, String capa) {
        this.nome = nome;
        this.descricao = descricao;
        this.capa = capa;
    }

    public Livro(String nome, String autor, String genero, String descricao, String capa, String usuarioId) {
        this.nome = nome;
        this.autor = autor;
        this.genero = genero;
        this.descricao = descricao;
        this.capa = capa;
        this.usuarioId = usuarioId;
        this.favorito = false;
        this.avaliacao = null;
    }
}