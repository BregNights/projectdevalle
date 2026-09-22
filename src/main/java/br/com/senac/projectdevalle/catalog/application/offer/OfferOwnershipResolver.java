package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProducerNotEligibleException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Evita duplicar, em cada caso de uso de edição de oferta, a resolução do producerId a partir do
// usuário autenticado e a checagem de posse — nunca deixar um produtor alterar a oferta de outro.
@Component
class OfferOwnershipResolver {

    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;

    OfferOwnershipResolver(OfferRepository offerRepository, ProducerDirectoryPort producerDirectoryPort) {
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
    }

    Offer resolveOwnedOffer(UUID offerId, UUID userId) {
        UUID producerId = producerDirectoryPort.findProducerIdByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));
        if (!offer.producerId().equals(producerId)) {
            throw new ResourceNotFoundException("Offer not found: " + offerId);
        }
        return offer;
    }

    // RN01 — editar, repor estoque ou reativar uma oferta exige produtor apto a operar. Pausar e remover
    // continuam liberados para o produtor suspenso, pois só reduzem o que está exposto no catálogo.
    Offer resolveOwnedOfferForEditing(UUID offerId, UUID userId) {
        Offer offer = resolveOwnedOffer(offerId, userId);
        if (!producerDirectoryPort.isEligibleToOperate(offer.producerId())) {
            throw new ProducerNotEligibleException("Producer is not approved to operate: " + offer.producerId());
        }
        return offer;
    }
}
