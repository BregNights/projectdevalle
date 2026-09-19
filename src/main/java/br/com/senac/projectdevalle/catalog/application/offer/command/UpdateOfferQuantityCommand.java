package br.com.senac.projectdevalle.catalog.application.offer.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateOfferQuantityCommand(UUID offerId, UUID userId, BigDecimal quantityAvailable) {
}
