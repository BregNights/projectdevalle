package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.AttachCertificationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Certification;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

// RF04 — produtor anexa certificações (orgânico, selo de origem, boas práticas de pesca, etc.)
// que ficam visíveis no perfil enquanto válidas (RN03, ver Certification.isValid).
@Service
public class AttachCertificationService {

    private final ProducerRepository producerRepository;
    private final Clock clock;

    public AttachCertificationService(ProducerRepository producerRepository, Clock clock) {
        this.producerRepository = producerRepository;
        this.clock = clock;
    }

    @Transactional
    public void attach(AttachCertificationCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));

        Certification certification = Certification.attach(command.type(), command.proofUrl(), command.validUntil());
        producer.attachCertification(certification, clock);

        producerRepository.save(producer);
    }
}
</content>
