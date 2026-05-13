package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Endereco;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ViaCepService {

    private final WebClient webClient;

    public ViaCepService(
            WebClient.Builder builder,
            @Value("${viacep.base-url:https://viacep.com.br}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
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