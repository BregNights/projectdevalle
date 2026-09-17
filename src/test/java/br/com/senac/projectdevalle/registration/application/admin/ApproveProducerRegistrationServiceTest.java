package br.com.senac.projectdevalle.registration.application.admin;

import br.com.senac.projectdevalle.registration.application.admin.command.ApproveProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import br.com.senac.projectdevalle.registration.domain.producer.OriginLocation;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApproveProducerRegistrationServiceTest {

    @Mock
    private ProducerRepository producerRepository;

    @Test
    void approvesProducerWhenWithinCoverageArea() {
        Producer producer = newProducer(new Coordinates(-26.9, -48.6));
        when(producerRepository.findById(producer.id())).thenReturn(Optional.of(producer));
        CoverageAreaPolicy alwaysCovers = address -> true;
        ApproveProducerRegistrationService service =
                new ApproveProducerRegistrationService(producerRepository, alwaysCovers);

        service.approve(new ApproveProducerRegistrationCommand(producer.id()));

        assertThat(producer.isEligibleToOperate()).isTrue();
    }

    @Test
    void throwsWhenProducerDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(producerRepository.findById(unknownId)).thenReturn(Optional.empty());
        ApproveProducerRegistrationService service =
                new ApproveProducerRegistrationService(producerRepository, address -> true);

        assertThatThrownBy(() -> service.approve(new ApproveProducerRegistrationCommand(unknownId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Producer newProducer(Coordinates coordinates) {
        Address address = new Address("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null);
        SupportingDocument document = SupportingDocument.issue(SupportingDocumentType.CPF, "12345678909",
                "https://files/doc.pdf");
        return Producer.register(UUID.randomUUID(), "Joao", new Cpf("12345678909"), ProductionType.FARMING,
                new OriginLocation(address, coordinates), List.of(document));
    }
}
