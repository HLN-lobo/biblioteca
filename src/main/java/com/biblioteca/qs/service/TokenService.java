package com.biblioteca.qs.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.biblioteca.qs.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Usuario usuario){

        try{

            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("biblioteca-api")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);

        } catch (Exception e){
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    public String validateToken(String token){

        try{

            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("biblioteca-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (Exception e){
            return "";
        }
    }

    private Instant generateExpirationDate(){
        return LocalDateTime.now()
                .plusHours(24)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}