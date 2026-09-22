package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.PublishOfferCommand;
import br.com.senac.projectdevalle.catalog.application.port.CatalogSettingsPort;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProducerNotEligibleException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProductCategoryNotEnabledException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class PublishOfferService {

    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;
    private final CatalogSettingsPort catalogSettingsPort;
    private final Clock clock;

    public PublishOfferService(OfferRepository offerRepository, ProducerDirectoryPort producerDirectoryPort,
                               CatalogSettingsPort catalogSettingsPort, Clock clock) {
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
        this.catalogSettingsPort = catalogSettingsPort;
        this.clock = clock;
    }

    // RF07/RF08 + RN01 — só um produtor aprovado pode publicar ofertas.
    @Transactional
    public Offer publish(PublishOfferCommand command) {
        UUID producerId = producerDirectoryPort.findProducerIdByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        if (!producerDirectoryPort.isEligibleToOperate(producerId)) {
            throw new ProducerNotEligibleException("Producer is not approved to operate: " + producerId);
        }
        // RF43 — só categorias habilitadas pela administração aceitam novas ofertas.
        if (command.category() != null && !catalogSettingsPort.enabledCategories().contains(command.category())) {
            throw new ProductCategoryNotEnabledException(command.category().name());
        }
        Offer offer = Offer.publish(producerId, command.productName(), command.category(), command.unit(),
                command.price(), command.quantityAvailable(), command.recurrence(), command.availabilityWindow(),
                command.photoUrls());
        offer.requireNotExpired(clock);
        return offerRepository.save(offer);
    }
}
