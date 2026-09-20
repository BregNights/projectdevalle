package br.com.senac.projectdevalle.registration.application.user.port;

public interface GoogleIdTokenVerifierPort {

    // Lança IllegalArgumentException se o token for inválido, expirado ou de outra aplicação.
    SocialIdentity verify(String idToken);
}
