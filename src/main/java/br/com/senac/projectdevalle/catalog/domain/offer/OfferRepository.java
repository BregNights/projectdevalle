package br.com.senac.projectdevalle.catalog.domain.offer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository {

    Offer save(Offer offer);

    Optional<Offer> findById(UUID id);

    // Leitura com lock pessimista: usada para reservar/devolver estoque sem perder atualizações concorrentes.
    Optional<Offer> findByIdForUpdate(UUID id);

    List<Offer> findByProducerId(UUID producerId);

    List<Offer> search(CatalogFilter filter);
}
