package com.biblioteca.qs.model;

import lombok.Data;

@Data
public class Endereco {
    private String cep;
    private String logradouro;
    private String bairro;
    private String localidade;
    private String uf;
}