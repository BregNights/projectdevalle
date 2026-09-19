package br.com.senac.projectdevalle.catalog.application.offer.command;

import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;
import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PublishOfferCommand(UUID userId, String productName, ProductCategory category, MeasurementUnit unit,
                                   BigDecimal price, BigDecimal quantityAvailable, Recurrence recurrence,
                                   AvailabilityWindow availabilityWindow, List<String> photoUrls) {
}
