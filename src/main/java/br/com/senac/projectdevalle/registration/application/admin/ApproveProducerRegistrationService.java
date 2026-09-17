package br.com.senac.projectdevalle.registration.application.admin;

import br.com.senac.projectdevalle.registration.application.admin.command.ApproveProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF03 — a transição de estado e as invariantes (RN01/RN02) vivem no agregado Producer;
// este serviço só orquestra: carregar, aprovar, persistir.
@Service
public class ApproveProducerRegistrationService {

    private final ProducerRepository producerRepository;
    private final CoverageAreaPolicy coverageAreaPolicy;

    public ApproveProducerRegistrationService(ProducerRepository producerRepository,
                                               CoverageAreaPolicy coverageAreaPolicy) {
        this.producerRepository = producerRepository;
        this.coverageAreaPolicy = coverageAreaPolicy;
    }

    @Transactional
    public void approve(ApproveProducerRegistrationCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));
        producer.approve(coverageAreaPolicy);
        producerRepository.save(producer);
    }
}
</content>
