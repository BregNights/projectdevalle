package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.OfferOwnershipCommand;
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
class ResumeOfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private OfferOwnershipResolver ownershipResolver;

    private ResumeOfferService service;

    @BeforeEach
    void setUp() {
        service = new ResumeOfferService(offerRepository, ownershipResolver);
    }

    @Test
    void resumesAPausedOfferBackToActiveAndPersistsIt() {
        UUID userId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        Offer offer = anActiveOffer();
        offer.pause();
        when(ownershipResolver.resolveOwnedOfferForEditing(offerId, userId)).thenReturn(offer);

        service.resume(new OfferOwnershipCommand(offerId, userId));

        assertThat(offer.status()).isEqualTo(OfferStatus.ACTIVE);
        verify(offerRepository).save(offer);
    }

    private static Offer anActiveOffer() {
        return Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }
}
