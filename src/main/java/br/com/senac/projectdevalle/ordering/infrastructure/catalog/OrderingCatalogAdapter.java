package br.com.senac.projectdevalle.ordering.infrastructure.catalog;

import br.com.senac.projectdevalle.catalog.application.port.CatalogSettingsPort;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Component
class OrderingCatalogAdapter implements OrderingCatalogPort {

    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;
    private final CatalogSettingsPort catalogSettingsPort;
    private final Clock clock;

    OrderingCatalogAdapter(OfferRepository offerRepository, ProducerDirectoryPort producerDirectoryPort,
                           CatalogSettingsPort catalogSettingsPort, Clock clock) {
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
        this.catalogSettingsPort = catalogSettingsPort;
        this.clock = clock;
    }

    @Override
    public Optional<OfferSnapshot> findOffer(UUID offerId) {
        return offerRepository.findById(offerId).map(this::toSnapshot);
    }

    @Override
    public void reserveStock(UUID offerId, BigDecimal quantity) {
        Offer offer = offerRepository.findByIdForUpdate(offerId)
                .orElseThrow(() -> new BusinessRuleViolationException("Offer no longer exists: " + offerId));
        offer.reserve(quantity);
        offerRepository.save(offer);
    }

    @Override
    public void releaseStock(UUID offerId, BigDecimal quantity) {
        offerRepository.findByIdForUpdate(offerId).ifPresent(offer -> {
            offer.release(quantity);
            offerRepository.save(offer);
        });
    }

    private OfferSnapshot toSnapshot(Offer offer) {
        boolean purchasable = offer.isVisibleInCatalog(clock)
                && catalogSettingsPort.enabledCategories().contains(offer.category())
                && producerDirectoryPort.isListable(offer.producerId());
        AvailabilityWindow window = offer.availabilityWindow();
        return new OfferSnapshot(offer.id(), offer.producerId(), offer.productName(), offer.category().name(),
                offer.unit().name(), offer.price(), offer.quantityAvailable(), window != null ? window.from() : null,
                window != null ? window.until() : null, purchasable);
    }
}
