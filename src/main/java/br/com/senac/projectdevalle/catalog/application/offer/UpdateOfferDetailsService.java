package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferDetailsCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class UpdateOfferDetailsService {

    private final OfferRepository offerRepository;
    private final OfferOwnershipResolver ownershipResolver;
    private final Clock clock;

    public UpdateOfferDetailsService(OfferRepository offerRepository, OfferOwnershipResolver ownershipResolver,
                                     Clock clock) {
        this.offerRepository = offerRepository;
        this.ownershipResolver = ownershipResolver;
        this.clock = clock;
    }

    @Transactional
    public void update(UpdateOfferDetailsCommand command) {
        Offer offer = ownershipResolver.resolveOwnedOfferForEditing(command.offerId(), command.userId());
        offer.updateDetails(command.price(), command.availabilityWindow(), command.photoUrls());
        offer.requireNotExpired(clock);
        offerRepository.save(offer);
    }
}
