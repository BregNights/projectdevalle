package br.com.senac.projectdevalle.registration.application.user.port;

public record SocialIdentity(String email, boolean emailVerified, String displayName) {
}
