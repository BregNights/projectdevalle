package br.com.senac.projectdevalle.catalog.infrastructure.persistence;

import br.com.senac.projectdevalle.catalog.domain.offer.CatalogFilter;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferStatus;
import org.springframework.data.jpa.domain.Specification;

final class OfferSpecifications {

    private OfferSpecifications() {
    }

    static Specification<OfferJpaEntity> fromFilter(CatalogFilter filter) {
        Specification<OfferJpaEntity> spec = (root, query, builder) ->
                builder.equal(root.get("status"), OfferStatus.ACTIVE);

        if (filter.category() != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("category"), filter.category()));
        }
        if (filter.producerId() != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("producerId"), filter.producerId()));
        }
        if (filter.producerIdIn() != null) {
            // Conjunto vazio = nenhum produtor elegível; evita gerar um "IN ()" inválido em SQL.
            spec = spec.and((root, query, builder) -> filter.producerIdIn().isEmpty()
                    ? builder.disjunction()
                    : root.get("producerId").in(filter.producerIdIn()));
        }
        if (filter.availableBy() != null) {
            spec = spec.and((root, query, builder) -> builder.or(
                    builder.isNull(root.get("availabilityFrom")),
                    builder.lessThanOrEqualTo(root.get("availabilityFrom"), filter.availableBy())));
        }
        if (filter.minPrice() != null) {
            spec = spec.and((root, query, builder) -> builder.ge(root.get("price"), filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            spec = spec.and((root, query, builder) -> builder.le(root.get("price"), filter.maxPrice()));
        }
        return spec;
    }
}
