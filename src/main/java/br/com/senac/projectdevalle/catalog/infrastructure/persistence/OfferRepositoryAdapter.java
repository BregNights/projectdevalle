package br.com.senac.projectdevalle.catalog.infrastructure.persistence;

import br.com.senac.projectdevalle.catalog.domain.offer.CatalogFilter;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.infrastructure.persistence.mapper.OfferEntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class OfferRepositoryAdapter implements OfferRepository {

    private final OfferJpaRepository jpaRepository;
    private final OfferEntityMapper mapper;

    OfferRepositoryAdapter(OfferJpaRepository jpaRepository, OfferEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Offer save(Offer offer) {
        OfferJpaEntity saved = jpaRepository.save(mapper.toEntity(offer));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Offer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Offer> findByProducerId(UUID producerId) {
        return jpaRepository.findByProducerId(producerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Offer> search(CatalogFilter filter) {
        return jpaRepository.findAll(OfferSpecifications.fromFilter(filter)).stream().map(mapper::toDomain).toList();
    }
}
