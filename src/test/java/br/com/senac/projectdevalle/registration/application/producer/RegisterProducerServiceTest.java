package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.RegisterProducerCommand;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterProducerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProducerRepository producerRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private GeocodingPort geocodingPort;

    private RegisterProducerService service;

    @BeforeEach
    void setUp() {
        service = new RegisterProducerService(userRepository, producerRepository, passwordHasher, geocodingPort);
        when(passwordHasher.hash(any())).thenReturn("hashed-password");
    }

    @Test
    void savesProducerWithResolvedCoordinatesWhenGeocodingSucceeds() {
        when(geocodingPort.geocode(any())).thenReturn(new Coordinates(-26.9, -48.6));

        RegisterProducerResult result = service.register(validCommand());

        assertThat(result.geocodingPending()).isFalse();
    }

    @Test
    void savesProducerAsGeocodingPendingWhenGeocodingFails() {
        when(geocodingPort.geocode(any())).thenThrow(new GeolocationUnavailableException("timeout"));

        RegisterProducerResult result = service.register(validCommand());

        assertThat(result.geocodingPending()).isTrue();
    }

    @Test
    void rejectsRegistrationWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> service.register(validCommand()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private RegisterProducerCommand validCommand() {
        Address address = new Address("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null);
        SupportingDocument document = SupportingDocument.issue(SupportingDocumentType.CPF, "12345678909",
                "https://files/doc.pdf");
        return new RegisterProducerCommand(new Email("joao@example.com"), "S3nhaForte!", "Joao",
                new Cpf("12345678909"), ProductionType.FARMING, address, List.of(document));
    }
}
