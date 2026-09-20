package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.UpdateProducerProfileCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProducerProfileService {

    private final ProducerRepository producerRepository;

    public UpdateProducerProfileService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Transactional
    public void update(UpdateProducerProfileCommand command) {
        Producer producer = producerRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        producer.updateName(command.name());
        producerRepository.save(producer);
    }
}
