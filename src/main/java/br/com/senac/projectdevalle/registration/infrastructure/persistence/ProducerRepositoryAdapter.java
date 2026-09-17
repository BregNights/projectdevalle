package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.mapper.ProducerEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class ProducerRepositoryAdapter implements ProducerRepository {

    private final ProducerJpaRepository jpaRepository;
    private final ProducerEntityMapper mapper;

    ProducerRepositoryAdapter(ProducerJpaRepository jpaRepository, ProducerEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Producer save(Producer producer) {
        ProducerJpaEntity saved = jpaRepository.save(mapper.toEntity(producer));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Producer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Producer> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }
}
