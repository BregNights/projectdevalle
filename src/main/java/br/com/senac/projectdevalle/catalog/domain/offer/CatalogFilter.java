package br.com.senac.projectdevalle.catalog.domain.offer;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

// RF10 — filtros de busca do catálogo. City/certificação são atributos do Produtor e são resolvidos na camada
// de aplicação para um conjunto de producerIdIn antes de chegar aqui.
public record CatalogFilter(ProductCategory category, UUID producerId, Set<UUID> producerIdIn,
                             BigDecimal minPrice, BigDecimal maxPrice) {
}
