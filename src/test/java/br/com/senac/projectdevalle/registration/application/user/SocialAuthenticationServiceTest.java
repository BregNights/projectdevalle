package br.com.senac.projectdevalle.registration.application.user;

import br.com.senac.projectdevalle.registration.application.user.command.SocialLoginCommand;
import br.com.senac.projectdevalle.registration.application.user.port.GoogleIdTokenVerifierPort;
import br.com.senac.projectdevalle.registration.application.user.port.SocialIdentity;
import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.Role;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenServicePort tokenServicePort;

    @Mock
    private GoogleIdTokenVerifierPort googleIdTokenVerifier;

    private SocialAuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new SocialAuthenticationService(userRepository, tokenServicePort, googleIdTokenVerifier);
    }

    @Test
    void issuesAccessTokenWhenEmailMatchesExistingUser() {
        User user = User.reconstitute(UUID.randomUUID(), new Email("joao@example.com"), "hash", Role.PRODUCER, true);
        when(googleIdTokenVerifier.verify(any())).thenReturn(
                new SocialIdentity("joao@example.com", true, "Joao"));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(tokenServicePort.issueAccessToken(user)).thenReturn("jwt-token");

        SocialAuthenticationResult result = service.authenticateWithGoogle(new SocialLoginCommand("id-token"));

        assertThat(result).isInstanceOf(SocialAuthenticationResult.Authenticated.class);
        assertThat(((SocialAuthenticationResult.Authenticated) result).accessToken()).isEqualTo("jwt-token");
    }

    @Test
    void returnsRegistrationRequiredWhenNoUserMatchesEmail() {
        when(googleIdTokenVerifier.verify(any())).thenReturn(
                new SocialIdentity("nova@example.com", true, "Nova"));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        SocialAuthenticationResult result = service.authenticateWithGoogle(new SocialLoginCommand("id-token"));

        assertThat(result).isInstanceOf(SocialAuthenticationResult.RegistrationRequired.class);
        var registrationRequired = (SocialAuthenticationResult.RegistrationRequired) result;
        assertThat(registrationRequired.email()).isEqualTo("nova@example.com");
        assertThat(registrationRequired.displayName()).isEqualTo("Nova");
    }

    @Test
    void rejectsUnverifiedEmail() {
        when(googleIdTokenVerifier.verify(any())).thenReturn(
                new SocialIdentity("joao@example.com", false, "Joao"));

        assertThatThrownBy(() -> service.authenticateWithGoogle(new SocialLoginCommand("id-token")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
