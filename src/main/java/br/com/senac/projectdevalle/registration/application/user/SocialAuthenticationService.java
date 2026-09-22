package br.com.senac.projectdevalle.registration.application.user;

import br.com.senac.projectdevalle.registration.application.user.command.SocialLoginCommand;
import br.com.senac.projectdevalle.registration.application.user.port.GoogleIdTokenVerifierPort;
import br.com.senac.projectdevalle.registration.application.user.port.SocialIdentity;
import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.registration.domain.user.exception.InvalidCredentialsException;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialAuthenticationService {

    private final UserRepository userRepository;
    private final TokenServicePort tokenServicePort;
    private final GoogleIdTokenVerifierPort googleIdTokenVerifier;

    public SocialAuthenticationService(UserRepository userRepository, TokenServicePort tokenServicePort,
                                        GoogleIdTokenVerifierPort googleIdTokenVerifier) {
        this.userRepository = userRepository;
        this.tokenServicePort = tokenServicePort;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

    @Transactional(readOnly = true)
    public SocialAuthenticationResult authenticateWithGoogle(SocialLoginCommand command) {
        return resolve(googleIdTokenVerifier.verify(command.idToken()));
    }

    private SocialAuthenticationResult resolve(SocialIdentity identity) {
        if (!identity.emailVerified()) {
            throw new IllegalArgumentException("Social account email is not verified");
        }

        return userRepository.findByEmail(new Email(identity.email()))
                .<SocialAuthenticationResult>map(user -> {
                    // Conta desativada (ex.: cadastro removido pela administração) não entra nem pelo login social.
                    if (!user.active()) {
                        throw new InvalidCredentialsException();
                    }
                    return new SocialAuthenticationResult.Authenticated(tokenServicePort.issueAccessToken(user));
                })
                .orElseGet(() ->
                        new SocialAuthenticationResult.RegistrationRequired(identity.email(), identity.displayName()));
    }
}
