package com.biblioteca.qs.controller;

import com.biblioteca.qs.model.LoginRequestDTO;
import com.biblioteca.qs.model.RegisterRequestDTO;
import com.biblioteca.qs.model.ResponseDTO;
import com.biblioteca.qs.model.Usuario;
import com.biblioteca.qs.repository.UsuarioRepository;
import com.biblioteca.qs.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService token;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO body){

        if(body.nome() == null || body.nome().isBlank()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "O nome é obrigatório"
                    ));
        }

        if(body.email() == null || body.email().isBlank()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "O email é obrigatório"
                    ));
        }

        if(body.senha() == null || body.senha().isBlank()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "A senha é obrigatória"
                    ));
        }

        if(body.senha().length() < 6){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "A senha deve ter no mínimo 6 caracteres"
                    ));
        }

        Optional<Usuario> user = repository.findByEmail(body.email());

        if(user.isPresent()){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Usuário já existe"
                    ));
        }

        Usuario newUser = new Usuario();

        newUser.setNome(body.nome());
        newUser.setEmail(body.email());
        newUser.setSenha(encoder.encode(body.senha()));

        repository.save(newUser);

        String token = this.token.generateToken(newUser);

        return ResponseEntity.ok(
                new ResponseDTO(
                        newUser.getNome(),
                        token
                )
        );
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {

        Optional<Usuario> userOptional = repository.findByEmail(body.email());

        if(userOptional.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Usuário não encontrado"
                    ));
        }

        Usuario user = userOptional.get();

        if(!encoder.matches(body.senha(), user.getSenha())){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Senha inválida"
                    ));
        }

        String token = this.token.generateToken(user);

        return ResponseEntity.ok(
                new ResponseDTO(
                        user.getNome(),
                        token
                )
        );
    }
}