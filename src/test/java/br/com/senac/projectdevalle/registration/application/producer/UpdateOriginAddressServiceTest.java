package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.UpdateOriginAddressCommand;
import br.com.senac.projectdevalle.registration.domain.producer.OriginLocation;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOriginAddressServiceTest {

    @Mock
    private ProducerRepository producerRepository;

    @Mock
    private GeocodingPort geocodingPort;

    private UpdateOriginAddressService service;

    @BeforeEach
    void setUp() {
        service = new UpdateOriginAddressService(producerRepository, geocodingPort);
    }

    @Test
    void updatesAddressAndResolvesCoordinatesWhenGeocodingSucceeds() {
        UUID userId = UUID.randomUUID();
        Producer producer = aProducer(userId);
        Address newAddress = new Address("Rua Nova", "50", "Bairro Novo", "Itajai", "SC", "88300-000", null);
        when(producerRepository.findByUserId(userId)).thenReturn(Optional.of(producer));
        when(geocodingPort.geocode(newAddress)).thenReturn(new Coordinates(-26.9, -48.6));

        boolean geocodingPending = service.update(new UpdateOriginAddressCommand(userId, newAddress));

        assertThat(geocodingPending).isFalse();
        assertThat(producer.originLocation().address()).isEqualTo(newAddress);
    }

    @Test
    void fallsBackToGeocodingPendingWhenGeocodingFails() {
        UUID userId = UUID.randomUUID();
        Producer producer = aProducer(userId);
        Address newAddress = new Address("Rua Nova", "50", "Bairro Novo", "Itajai", "SC", "88300-000", null);
        when(producerRepository.findByUserId(userId)).thenReturn(Optional.of(producer));
        when(geocodingPort.geocode(any())).thenThrow(new GeolocationUnavailableException("timeout"));

        boolean geocodingPending = service.update(new UpdateOriginAddressCommand(userId, newAddress));

        assertThat(geocodingPending).isTrue();
    }

    @Test
    void rejectsWhenNoProducerIsRegisteredForCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(producerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateOriginAddressCommand(userId, aValidAddress())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Producer aProducer(UUID userId) {
        Address address = new Address("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null);
        SupportingDocument document = SupportingDocument.issue(SupportingDocumentType.CPF, "12345678909",
                "https://files/doc.pdf");
        return Producer.register(userId, "Joao", new Cpf("12345678909"), ProductionType.FARMING,
                new OriginLocation(address, new Coordinates(-26.9, -48.6)), List.of(document));
    }

    private static Address aValidAddress() {
        return new Address("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null);
    }
}
