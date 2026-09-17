package br.com.senac.projectdevalle.registration.domain.producer;

import java.util.Optional;
import java.util.UUID;

public interface ProducerRepository {

    Producer save(Producer producer);

    Optional<Producer> findById(UUID id);
}
