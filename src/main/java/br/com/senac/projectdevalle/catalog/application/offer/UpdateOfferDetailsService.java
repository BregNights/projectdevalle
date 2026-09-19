package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferDetailsCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOfferDetailsService {

    private final OfferRepository offerRepository;
    private final OfferOwnershipResolver ownershipResolver;

    public UpdateOfferDetailsService(OfferRepository offerRepository, OfferOwnershipResolver ownershipResolver) {
        this.offerRepository = offerRepository;
        this.ownershipResolver = ownershipResolver;
    }

    @Transactional
    public void update(UpdateOfferDetailsCommand command) {
        Offer offer = ownershipResolver.resolveOwnedOffer(command.offerId(), command.userId());
        offer.updateDetails(command.price(), command.availabilityWindow(), command.photoUrls());
        offerRepository.save(offer);
    }
}
