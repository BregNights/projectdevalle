package br.com.senac.projectdevalle.registration.domain.producer;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducerRepository {

    Producer save(Producer producer);

    Optional<Producer> findById(UUID id);

    Optional<Producer> findByUserId(UUID userId);

    List<Producer> findByStatus(RegistrationStatus status);
}
