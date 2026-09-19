package br.com.senac.projectdevalle.catalog.application.offer.command;

import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateOfferDetailsCommand(UUID offerId, UUID userId, BigDecimal price,
                                         AvailabilityWindow availabilityWindow, List<String> photoUrls) {
}
