package br.com.senac.projectdevalle.catalog.application.catalog;

import br.com.senac.projectdevalle.catalog.application.port.ProducerCatalogInfo;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.shared.application.port.EstimatedDistance;

// distance é nulo quando o requisitante não é um restaurante autenticado com endereço geocodificado,
// ou quando o cálculo de distância falhou (RNF11 — nunca bloqueia a busca por isso).
public record CatalogEntry(Offer offer, ProducerCatalogInfo producer, EstimatedDistance distance) {
}
