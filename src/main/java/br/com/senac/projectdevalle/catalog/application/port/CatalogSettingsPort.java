package br.com.senac.projectdevalle.catalog.application.port;

import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;

import java.util.Optional;
import java.util.Set;

// RF43 — parâmetros da plataforma que afetam o catálogo, sem depender dos tipos do módulo platform.
public interface CatalogSettingsPort {

    Set<ProductCategory> enabledCategories();

    // RF10 — municípios de uma região atendida; vazio se a região não existir.
    Optional<Set<String>> citiesOfRegion(String region);
}
