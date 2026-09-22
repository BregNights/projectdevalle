package br.com.senac.projectdevalle.catalog.domain.offer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// RF10 — filtros de busca do catálogo. City/certificação são atributos do Produtor e são resolvidos na camada
// de aplicação para um conjunto de producerIdIn antes de chegar aqui. availableBy é o prazo de que o
// restaurante precisa: só entram ofertas cuja disponibilidade começa até essa data.
public record CatalogFilter(ProductCategory category, UUID producerId, Set<UUID> producerIdIn,
                             BigDecimal minPrice, BigDecimal maxPrice, LocalDate availableBy) {
}
