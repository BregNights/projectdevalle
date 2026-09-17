package br.com.senac.projectdevalle.registration.application.user;

import br.com.senac.projectdevalle.registration.application.user.command.AuthenticateUserCommand;
import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.registration.domain.user.exception.InvalidCredentialsException;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenServicePort tokenServicePort;

    public AuthenticateUserService(UserRepository userRepository, PasswordHasher passwordHasher,
                                    TokenServicePort tokenServicePort) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenServicePort = tokenServicePort;
    }

    @Transactional(readOnly = true)
    public String authenticate(AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(InvalidCredentialsException::new);
        user.authenticate(command.rawPassword(), passwordHasher);
        return tokenServicePort.issueAccessToken(user);
    }
}
