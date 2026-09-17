package br.com.senac.projectdevalle.registration.domain.user;

import br.com.senac.projectdevalle.shared.domain.vo.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
