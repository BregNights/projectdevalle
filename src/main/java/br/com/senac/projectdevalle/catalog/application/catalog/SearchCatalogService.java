package br.com.senac.projectdevalle.catalog.application.catalog;

import br.com.senac.projectdevalle.catalog.application.catalog.command.SearchCatalogCommand;
import br.com.senac.projectdevalle.catalog.application.port.ProducerCatalogInfo;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.application.port.RestaurantDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.CatalogFilter;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.shared.application.port.DistanceCalculationPort;
import br.com.senac.projectdevalle.shared.application.port.EstimatedDistance;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// RF10 (busca/filtros do catálogo) + RF11 (distância até o restaurante, quando aplicável).
@Service
public class SearchCatalogService {

    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;
    private final RestaurantDirectoryPort restaurantDirectoryPort;
    private final DistanceCalculationPort distanceCalculationPort;
    private final Clock clock;

    public SearchCatalogService(OfferRepository offerRepository, ProducerDirectoryPort producerDirectoryPort,
                                 RestaurantDirectoryPort restaurantDirectoryPort,
                                 DistanceCalculationPort distanceCalculationPort, Clock clock) {
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
        this.restaurantDirectoryPort = restaurantDirectoryPort;
        this.distanceCalculationPort = distanceCalculationPort;
        this.clock = clock;
    }

    public List<CatalogEntry> search(SearchCatalogCommand command) {
        CatalogFilter filter = buildFilter(command);
        Coordinates requesterCoordinates = resolveRequesterCoordinates(command.requesterUserId());

        return offerRepository.search(filter).stream()
                .filter(offer -> offer.isVisibleInCatalog(clock))
                .map(offer -> toCatalogEntry(offer, requesterCoordinates))
                .toList();
    }

    private CatalogFilter buildFilter(SearchCatalogCommand command) {
        if (command.producerId() != null) {
            return new CatalogFilter(command.category(), command.producerId(), null, command.minPrice(),
                    command.maxPrice());
        }
        if (command.city() != null || command.certificationType() != null) {
            Set<UUID> eligibleProducerIds = producerDirectoryPort.findEligibleProducerIds(command.city(),
                    command.certificationType());
            return new CatalogFilter(command.category(), null, eligibleProducerIds, command.minPrice(),
                    command.maxPrice());
        }
        return new CatalogFilter(command.category(), null, null, command.minPrice(), command.maxPrice());
    }

    private Coordinates resolveRequesterCoordinates(UUID requesterUserId) {
        if (requesterUserId == null) {
            return null;
        }
        return restaurantDirectoryPort.findRestaurantIdByUserId(requesterUserId)
                .flatMap(restaurantDirectoryPort::findPrimaryAddressCoordinates)
                .orElse(null);
    }

    private CatalogEntry toCatalogEntry(Offer offer, Coordinates requesterCoordinates) {
        ProducerCatalogInfo producer = producerDirectoryPort.findById(offer.producerId()).orElse(null);
        EstimatedDistance distance = computeDistance(requesterCoordinates, producer);
        return new CatalogEntry(offer, producer, distance);
    }

    // RNF11 — falha ou indisponibilidade do cálculo de distância nunca impede a exibição da oferta.
    private EstimatedDistance computeDistance(Coordinates requesterCoordinates, ProducerCatalogInfo producer) {
        if (requesterCoordinates == null || producer == null || producer.coordinates() == null) {
            return null;
        }
        try {
            return distanceCalculationPort.calculate(requesterCoordinates, producer.coordinates());
        } catch (GeolocationUnavailableException exception) {
            return null;
        }
    }
}
