package br.com.senac.projectdevalle.registration.application.user;

public sealed interface SocialAuthenticationResult {

    record Authenticated(String accessToken) implements SocialAuthenticationResult {
    }

    record RegistrationRequired(String email, String displayName) implements SocialAuthenticationResult {
    }
}
