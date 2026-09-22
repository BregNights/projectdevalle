package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferQuantityCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferStatus;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOfferQuantityServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private OfferOwnershipResolver ownershipResolver;

    private UpdateOfferQuantityService service;

    @BeforeEach
    void setUp() {
        service = new UpdateOfferQuantityService(offerRepository, ownershipResolver);
    }

    // RN04
    @Test
    void zeroingQuantityMarksOfferAsSoldOutAndPersistsIt() {
        UUID userId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        Offer offer = anActiveOffer();
        when(ownershipResolver.resolveOwnedOfferForEditing(offerId, userId)).thenReturn(offer);

        service.update(new UpdateOfferQuantityCommand(offerId, userId, BigDecimal.ZERO));

        assertThat(offer.status()).isEqualTo(OfferStatus.SOLD_OUT);
        verify(offerRepository).save(offer);
    }

    private static Offer anActiveOffer() {
        return Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }
}
