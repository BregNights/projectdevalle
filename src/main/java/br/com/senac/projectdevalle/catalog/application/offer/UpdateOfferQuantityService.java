package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferQuantityCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOfferQuantityService {

    private final OfferRepository offerRepository;
    private final OfferOwnershipResolver ownershipResolver;

    public UpdateOfferQuantityService(OfferRepository offerRepository, OfferOwnershipResolver ownershipResolver) {
        this.offerRepository = offerRepository;
        this.ownershipResolver = ownershipResolver;
    }

    // RN04 — atualização de estoque, com transição automática para/de SOLD_OUT.
    @Transactional
    public void update(UpdateOfferQuantityCommand command) {
        Offer offer = ownershipResolver.resolveOwnedOffer(command.offerId(), command.userId());
        offer.updateQuantity(command.quantityAvailable());
        offerRepository.save(offer);
    }
}
