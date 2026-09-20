package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

public record SocialLoginResponse(String status, String accessToken, String email, String displayName) {

    public static SocialLoginResponse authenticated(String accessToken) {
        return new SocialLoginResponse("AUTHENTICATED", accessToken, null, null);
    }

    public static SocialLoginResponse registrationRequired(String email, String displayName) {
        return new SocialLoginResponse("REGISTRATION_REQUIRED", null, email, displayName);
    }
}
