package br.com.senac.projectdevalle.registration.domain.user;

import br.com.senac.projectdevalle.registration.domain.user.exception.InvalidCredentialsException;
import br.com.senac.projectdevalle.shared.domain.vo.Email;

import java.util.UUID;

public class User {

    private final UUID id;
    private final Email email;
    private String passwordHash;
    private final Role role;
    private boolean active;

    private User(UUID id, Email email, String passwordHash, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public static User register(Email email, String rawPassword, Role role, PasswordHasher passwordHasher) {
        return new User(UUID.randomUUID(), email, passwordHasher.hash(rawPassword), role, true);
    }

    public static User reconstitute(UUID id, Email email, String passwordHash, Role role, boolean active) {
        return new User(id, email, passwordHash, role, active);
    }

    public void authenticate(String rawPassword, PasswordHasher passwordHasher) {
        if (!active || !passwordHasher.matches(rawPassword, passwordHash)) {
            throw new InvalidCredentialsException();
        }
    }

    public void resetPassword(String newRawPassword, PasswordHasher passwordHasher) {
        this.passwordHash = passwordHasher.hash(newRawPassword);
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public boolean active() {
        return active;
    }
}
</content>
