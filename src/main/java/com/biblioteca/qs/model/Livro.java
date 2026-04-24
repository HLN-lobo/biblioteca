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

    public Livro() {}

    public Livro(String nome, String descricao, String capa) {
        this.nome = nome;
        this.descricao = descricao;
        this.capa = capa;
    }

}