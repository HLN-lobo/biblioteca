package com.biblioteca.qs.service;

import com.biblioteca.qs.model.Endereco;
import com.biblioteca.qs.model.Usuario;
import com.biblioteca.qs.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ViaCepService viaCepService;

    public Usuario cadastrar(String email, String nome, String senha, String cep) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email já cadastrado: " + email);
        }
        Usuario usuario = new Usuario(email, nome, passwordEncoder.encode(senha), cep);

        if (cep != null && !cep.isBlank()) {
            Endereco endereco = viaCepService.buscarEndereco(cep);
            usuario.setEndereco(endereco);
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
    }
}