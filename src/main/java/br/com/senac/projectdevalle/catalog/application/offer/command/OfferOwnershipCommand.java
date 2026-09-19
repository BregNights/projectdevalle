package br.com.senac.projectdevalle.catalog.application.offer.command;

import java.util.UUID;

// Comando para ações que só exigem identificar a oferta e o produtor autenticado (pause/resume/remove).
public record OfferOwnershipCommand(UUID offerId, UUID userId) {
}
