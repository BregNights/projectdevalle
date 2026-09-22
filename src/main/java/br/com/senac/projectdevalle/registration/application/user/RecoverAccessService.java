package br.com.senac.projectdevalle.registration.application.user;

import br.com.senac.projectdevalle.registration.application.user.command.RequestPasswordResetCommand;
import br.com.senac.projectdevalle.registration.application.user.command.ResetPasswordCommand;
import br.com.senac.projectdevalle.registration.application.user.port.PasswordResetNotificationPort;
import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// RF06 — recuperação de acesso: o link com o token de redefinição é enviado ao e-mail da conta.
@Service
public class RecoverAccessService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenServicePort tokenServicePort;
    private final PasswordResetNotificationPort passwordResetNotificationPort;

    public RecoverAccessService(UserRepository userRepository, PasswordHasher passwordHasher,
                                 TokenServicePort tokenServicePort,
                                 PasswordResetNotificationPort passwordResetNotificationPort) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenServicePort = tokenServicePort;
        this.passwordResetNotificationPort = passwordResetNotificationPort;
    }

    @Transactional(readOnly = true)
    public void requestReset(RequestPasswordResetCommand command) {
        // RN39 — o chamador recebe sempre a mesma resposta, exista ou não a conta (evita enumeração de usuários).
        // Contas desativadas (cadastro removido, RN52) não recebem link.
        userRepository.findByEmail(new Email(command.email()))
                .filter(User::active)
                .ifPresent(user -> passwordResetNotificationPort.sendPasswordResetLink(user.email(),
                        tokenServicePort.issuePasswordResetToken(user)));
    }

    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        UUID userId = tokenServicePort.parsePasswordResetSubject(command.resetToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.resetPassword(command.newRawPassword(), passwordHasher);
        userRepository.save(user);
    }
}
