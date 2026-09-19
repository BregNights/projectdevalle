package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.PublishOfferCommand;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProducerNotEligibleException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PublishOfferService {

    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;

    public PublishOfferService(OfferRepository offerRepository, ProducerDirectoryPort producerDirectoryPort) {
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
    }

    // RF07/RF08 + RN01 — só um produtor aprovado pode publicar ofertas.
    @Transactional
    public Offer publish(PublishOfferCommand command) {
        UUID producerId = producerDirectoryPort.findProducerIdByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        if (!producerDirectoryPort.isEligibleToOperate(producerId)) {
            throw new ProducerNotEligibleException("Producer is not approved to operate: " + producerId);
        }
        Offer offer = Offer.publish(producerId, command.productName(), command.category(), command.unit(),
                command.price(), command.quantityAvailable(), command.recurrence(), command.availabilityWindow(),
                command.photoUrls());
        return offerRepository.save(offer);
    }
}
