package br.com.senac.projectdevalle.catalog.infrastructure.persistence.mapper;

import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import br.com.senac.projectdevalle.catalog.domain.offer.RecurrenceType;
import br.com.senac.projectdevalle.catalog.infrastructure.persistence.OfferJpaEntity;
import org.springframework.stereotype.Component;

// Mapeamento manual (não MapStruct): Offer é um agregado com construtor privado (reconstitute).
@Component
public class OfferEntityMapper {

    public OfferJpaEntity toEntity(Offer offer) {
        Recurrence recurrence = offer.recurrence();
        AvailabilityWindow availabilityWindow = offer.availabilityWindow();

        return OfferJpaEntity.builder()
                .id(offer.id())
                .producerId(offer.producerId())
                .productName(offer.productName())
                .category(offer.category())
                .unit(offer.unit())
                .price(offer.price())
                .quantityAvailable(offer.quantityAvailable())
                .recurrenceType(recurrence.type())
                .recurrenceDayOfWeek(recurrence.dayOfWeek())
                .availabilityFrom(availabilityWindow != null ? availabilityWindow.from() : null)
                .availabilityUntil(availabilityWindow != null ? availabilityWindow.until() : null)
                .photoUrls(offer.photoUrls())
                .status(offer.status())
                .build();
    }

    public Offer toDomain(OfferJpaEntity entity) {
        Recurrence recurrence = entity.getRecurrenceType() == RecurrenceType.RECURRING
                ? Recurrence.weekly(entity.getRecurrenceDayOfWeek())
                : Recurrence.oneTime();

        AvailabilityWindow availabilityWindow = entity.getAvailabilityFrom() != null
                || entity.getAvailabilityUntil() != null
                ? new AvailabilityWindow(entity.getAvailabilityFrom(), entity.getAvailabilityUntil())
                : null;

        return Offer.reconstitute(entity.getId(), entity.getProducerId(), entity.getProductName(),
                entity.getCategory(), entity.getUnit(), entity.getPrice(), entity.getQuantityAvailable(),
                recurrence, availabilityWindow, entity.getPhotoUrls(), entity.getStatus());
    }
}
