package br.com.senac.projectdevalle.registration.application.user;

import br.com.senac.projectdevalle.registration.application.user.command.RequestPasswordResetCommand;
import br.com.senac.projectdevalle.registration.application.user.port.PasswordResetNotificationPort;
import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoverAccessServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenServicePort tokenServicePort;

    @Mock
    private PasswordResetNotificationPort passwordResetNotificationPort;

    private RecoverAccessService service;

    @BeforeEach
    void setUp() {
        service = new RecoverAccessService(userRepository, passwordHasher, tokenServicePort,
                passwordResetNotificationPort);
    }

    // RF06 — o link de redefinição é enviado ao e-mail da conta.
    @Test
    void sendsResetLinkToTheAccountEmail() {
        User user = User.reconstitute(UUID.randomUUID(), new Email("joao@example.com"), "hash", Role.PRODUCER, true);
        when(userRepository.findByEmail(new Email("joao@example.com"))).thenReturn(Optional.of(user));
        when(tokenServicePort.issuePasswordResetToken(user)).thenReturn("reset-token");

        service.requestReset(new RequestPasswordResetCommand("joao@example.com"));

        verify(passwordResetNotificationPort).sendPasswordResetLink(user.email(), "reset-token");
    }

    // RN39 — sem conta, nada é enviado e nada muda na resposta.
    @Test
    void doesNothingWhenEmailIsNotRegistered() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        service.requestReset(new RequestPasswordResetCommand("ninguem@example.com"));

        verifyNoInteractions(tokenServicePort, passwordResetNotificationPort);
    }

    // RN52 — conta desativada (cadastro removido) não recebe link.
    @Test
    void doesNotSendLinkToDeactivatedAccounts() {
        User user = User.reconstitute(UUID.randomUUID(), new Email("joao@example.com"), "hash", Role.PRODUCER, false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        service.requestReset(new RequestPasswordResetCommand("joao@example.com"));

        verifyNoInteractions(tokenServicePort, passwordResetNotificationPort);
    }
}
