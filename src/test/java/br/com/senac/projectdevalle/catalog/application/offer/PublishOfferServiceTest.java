package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.PublishOfferCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishOfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ProducerDirectoryPort producerDirectoryPort;

    private PublishOfferService service;

    @BeforeEach
    void setUp() {
        service = new PublishOfferService(offerRepository, producerDirectoryPort);
    }

    @Test
    void rejectsPublishingWhenProducerIsNotEligibleToOperate() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(false);

        assertThatThrownBy(() -> service.publish(validCommand(userId)))
                .isInstanceOf(ProducerNotEligibleException.class);
    }

    @Test
    void rejectsPublishingWhenNoProducerIsRegisteredForCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(validCommand(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void publishesOfferForEligibleProducer() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(true);
        when(offerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Offer offer = service.publish(validCommand(userId));

        assertThat(offer.producerId()).isEqualTo(producerId);
    }

    private PublishOfferCommand validCommand(UUID userId) {
        return new PublishOfferCommand(userId, "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }
}
