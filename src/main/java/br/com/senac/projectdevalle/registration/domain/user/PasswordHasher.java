package br.com.senac.projectdevalle.registration.domain.user;

// Porta definida pelo dominio para nao acoplar o agregado User a uma lib de hashing especifica.
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
</content>
