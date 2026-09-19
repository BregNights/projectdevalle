package br.com.senac.projectdevalle.catalog.infrastructure.web.dto;

import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferStatus;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.RecurrenceType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        UUID producerId,
        String productName,
        ProductCategory category,
        MeasurementUnit unit,
        BigDecimal price,
        BigDecimal quantityAvailable,
        RecurrenceType recurrenceType,
        DayOfWeek recurrenceDayOfWeek,
        LocalDate availabilityFrom,
        LocalDate availabilityUntil,
        List<String> photoUrls,
        OfferStatus status
) {

    public static OfferResponse from(Offer offer) {
        return new OfferResponse(
                offer.id(),
                offer.producerId(),
                offer.productName(),
                offer.category(),
                offer.unit(),
                offer.price(),
                offer.quantityAvailable(),
                offer.recurrence().type(),
                offer.recurrence().dayOfWeek(),
                offer.availabilityWindow() != null ? offer.availabilityWindow().from() : null,
                offer.availabilityWindow() != null ? offer.availabilityWindow().until() : null,
                offer.photoUrls(),
                offer.status());
    }
}
