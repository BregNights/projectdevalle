package br.com.senac.projectdevalle.catalog.infrastructure.web.dto;

import br.com.senac.projectdevalle.catalog.application.catalog.CatalogEntry;

import java.util.UUID;

public record CatalogEntryResponse(
        OfferResponse offer,
        UUID producerId,
        String producerName,
        String producerCity,
        Double distanceKilometers,
        Long distanceDurationMinutes
) {

    public static CatalogEntryResponse from(CatalogEntry entry) {
        return new CatalogEntryResponse(
                OfferResponse.from(entry.offer()),
                entry.producer() != null ? entry.producer().producerId() : entry.offer().producerId(),
                entry.producer() != null ? entry.producer().name() : null,
                entry.producer() != null ? entry.producer().city() : null,
                entry.distance() != null ? entry.distance().kilometers() : null,
                entry.distance() != null ? entry.distance().duration().toMinutes() : null);
    }
}
