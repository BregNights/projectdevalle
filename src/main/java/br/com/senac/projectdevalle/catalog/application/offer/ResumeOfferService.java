package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.OfferOwnershipCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeOfferService {

    private final OfferRepository offerRepository;
    private final OfferOwnershipResolver ownershipResolver;

    public ResumeOfferService(OfferRepository offerRepository, OfferOwnershipResolver ownershipResolver) {
        this.offerRepository = offerRepository;
        this.ownershipResolver = ownershipResolver;
    }

    @Transactional
    public void resume(OfferOwnershipCommand command) {
        Offer offer = ownershipResolver.resolveOwnedOfferForEditing(command.offerId(), command.userId());
        offer.resume();
        offerRepository.save(offer);
    }
}
