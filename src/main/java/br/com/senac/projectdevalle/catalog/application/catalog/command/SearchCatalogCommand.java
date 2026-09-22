package br.com.senac.projectdevalle.catalog.application.catalog.command;

import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// requesterUserId é nulo para busca anônima/sem restaurante autenticado (RF11 fica indisponível nesse caso).
public record SearchCatalogCommand(ProductCategory category, UUID producerId, String region, String city,
                                    String certificationType, BigDecimal minPrice, BigDecimal maxPrice,
                                    LocalDate availableBy, UUID requesterUserId) {
}
