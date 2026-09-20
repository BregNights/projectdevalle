package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.user.AuthenticateUserService;
import br.com.senac.projectdevalle.registration.application.user.RecoverAccessService;
import br.com.senac.projectdevalle.registration.application.user.SocialAuthenticationResult;
import br.com.senac.projectdevalle.registration.application.user.SocialAuthenticationService;
import br.com.senac.projectdevalle.registration.application.user.command.AuthenticateUserCommand;
import br.com.senac.projectdevalle.registration.application.user.command.RequestPasswordResetCommand;
import br.com.senac.projectdevalle.registration.application.user.command.ResetPasswordCommand;
import br.com.senac.projectdevalle.registration.application.user.command.SocialLoginCommand;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RequestPasswordResetRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ResetPasswordRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SocialLoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SocialLoginResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TokenResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticateUserService authenticateUserService;
    private final RecoverAccessService recoverAccessService;
    private final SocialAuthenticationService socialAuthenticationService;

    public AuthenticationController(AuthenticateUserService authenticateUserService,
                                     RecoverAccessService recoverAccessService,
                                     SocialAuthenticationService socialAuthenticationService) {
        this.authenticateUserService = authenticateUserService;
        this.recoverAccessService = recoverAccessService;
        this.socialAuthenticationService = socialAuthenticationService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        String accessToken = authenticateUserService.authenticate(
                new AuthenticateUserCommand(request.email(), request.password()));
        return new TokenResponse(accessToken);
    }

    @PostMapping("/google")
    public SocialLoginResponse loginWithGoogle(@Valid @RequestBody SocialLoginRequest request) {
        return toResponse(socialAuthenticationService.authenticateWithGoogle(
                new SocialLoginCommand(request.idToken())));
    }

    private SocialLoginResponse toResponse(SocialAuthenticationResult result) {
        return switch (result) {
            case SocialAuthenticationResult.Authenticated authenticated ->
                    SocialLoginResponse.authenticated(authenticated.accessToken());
            case SocialAuthenticationResult.RegistrationRequired registrationRequired ->
                    SocialLoginResponse.registrationRequired(
                            registrationRequired.email(), registrationRequired.displayName());
        };
    }

    // RF06 — o token de reset é entregue por e-mail/WhatsApp pelo futuro módulo de notificações
    // (RF38); por ora apenas registramos em log, nunca o devolvemos na resposta HTTP.
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/password-reset/request")
    public void requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
        recoverAccessService.requestReset(new RequestPasswordResetCommand(request.email()))
                .ifPresent(token -> log.info("Password reset token issued for {}: {}", request.email(), token));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/password-reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        recoverAccessService.resetPassword(new ResetPasswordCommand(request.resetToken(), request.newPassword()));
    }
}
