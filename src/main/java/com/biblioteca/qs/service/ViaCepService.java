package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Endereco;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ViaCepService {

    private final WebClient webClient;

    @Autowired
    public ViaCepService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://viacep.com.br")
                .build();
    }

    public ViaCepService(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Endereco buscarEndereco(String cep) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");

        if (cepLimpo.length() != 8) {
            throw new IllegalArgumentException("CEP inválido: " + cep);
        }

        Endereco endereco = webClient.get()
                .uri("/ws/{cep}/json/", cepLimpo)
                .retrieve()
                .bodyToMono(Endereco.class)
                .block();

        if (endereco == null || endereco.getCep() == null) {
            throw new RuntimeException("CEP não encontrado: " + cep);
        }

        return endereco;
    }
}