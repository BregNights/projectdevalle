package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProducerNotEligibleException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferOwnershipResolverTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ProducerDirectoryPort producerDirectoryPort;

    private OfferOwnershipResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new OfferOwnershipResolver(offerRepository, producerDirectoryPort);
    }

    @Test
    void resolvesOfferWhenItBelongsToTheRequestingProducer() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        Offer offer = anOfferOwnedBy(producerId);
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(offerRepository.findById(offer.id())).thenReturn(Optional.of(offer));

        Offer resolved = resolver.resolveOwnedOffer(offer.id(), userId);

        assertThat(resolved).isSameAs(offer);
    }

    @Test
    void rejectsWhenNoProducerIsRegisteredForCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveOwnedOffer(UUID.randomUUID(), userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsWhenOfferDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(UUID.randomUUID()));
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveOwnedOffer(offerId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Regra de segurança central: nunca deixar um produtor alterar a oferta de outro.
    @Test
    void rejectsWhenOfferBelongsToADifferentProducer() {
        UUID userId = UUID.randomUUID();
        UUID requestingProducerId = UUID.randomUUID();
        Offer offer = anOfferOwnedBy(UUID.randomUUID());
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(requestingProducerId));
        when(offerRepository.findById(offer.id())).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> resolver.resolveOwnedOffer(offer.id(), userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // RN01 — produtor suspenso não pode editar/repor/reativar ofertas.
    @Test
    void rejectsEditingWhenProducerIsNotEligibleToOperate() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        Offer offer = anOfferOwnedBy(producerId);
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(offerRepository.findById(offer.id())).thenReturn(Optional.of(offer));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(false);

        assertThatThrownBy(() -> resolver.resolveOwnedOfferForEditing(offer.id(), userId))
                .isInstanceOf(ProducerNotEligibleException.class);
    }

    @Test
    void allowsEditingWhenProducerIsEligibleToOperate() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        Offer offer = anOfferOwnedBy(producerId);
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(offerRepository.findById(offer.id())).thenReturn(Optional.of(offer));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(true);

        assertThat(resolver.resolveOwnedOfferForEditing(offer.id(), userId)).isSameAs(offer);
    }

    private static Offer anOfferOwnedBy(UUID producerId) {
        return Offer.publish(producerId, "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }
}
