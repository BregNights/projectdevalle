package br.com.senac.projectdevalle.registration.application.user;

import br.com.senac.projectdevalle.registration.application.user.command.RequestPasswordResetCommand;
import br.com.senac.projectdevalle.registration.application.user.command.ResetPasswordCommand;
import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

// RF06 — recuperação de acesso. O envio do token por e-mail/WhatsApp fica a cargo do futuro
// módulo de notificações (RF38); por ora o token de reset é apenas emitido por esta camada.
@Service
public class RecoverAccessService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenServicePort tokenServicePort;

    public RecoverAccessService(UserRepository userRepository, PasswordHasher passwordHasher,
                                 TokenServicePort tokenServicePort) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenServicePort = tokenServicePort;
    }

    @Transactional(readOnly = true)
    public Optional<String> requestReset(RequestPasswordResetCommand command) {
        // Não revela se o e-mail existe ou não, para evitar enumeração de usuários.
        return userRepository.findByEmail(new Email(command.email()))
                .map(tokenServicePort::issuePasswordResetToken);
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
