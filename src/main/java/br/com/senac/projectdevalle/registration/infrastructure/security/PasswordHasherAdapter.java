package br.com.senac.projectdevalle.registration.infrastructure.security;

import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class PasswordHasherAdapter implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    PasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return passwordEncoder.matches(rawPassword, hash);
    }
}
