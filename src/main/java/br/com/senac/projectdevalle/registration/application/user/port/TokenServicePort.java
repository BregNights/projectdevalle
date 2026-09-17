package br.com.senac.projectdevalle.registration.application.user.port;

import br.com.senac.projectdevalle.registration.domain.user.User;

import java.util.UUID;

public interface TokenServicePort {

    String issueAccessToken(User user);

    String issuePasswordResetToken(User user);

    // Lança IllegalArgumentException se o token for inválido, expirado ou não for de reset de senha.
    UUID parsePasswordResetSubject(String token);
}
