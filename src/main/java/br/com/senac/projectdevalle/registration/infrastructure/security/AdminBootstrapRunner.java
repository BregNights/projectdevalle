package br.com.senac.projectdevalle.registration.infrastructure.security;

import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.Role;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

// RF03/RNF08 — garante que exista um Administrador para operar o fluxo de aprovação.
// Só roda se ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD estiverem definidas, e é idempotente
// (não recria nem sobrescreve o usuário em reinicializações subsequentes).
@Component
@EnableConfigurationProperties(AdminBootstrapProperties.class)
class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AdminBootstrapProperties properties;

    AdminBootstrapRunner(UserRepository userRepository, PasswordHasher passwordHasher,
                          AdminBootstrapProperties properties) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isConfigured()) {
            log.info("ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD não definidas — nenhum administrador criado");
            return;
        }

        Email email = new Email(properties.email());
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = User.register(email, properties.password(), Role.ADMINISTRATOR, passwordHasher);
        userRepository.save(admin);
        log.info("Administrador inicial criado: {}", properties.email());
    }
}
