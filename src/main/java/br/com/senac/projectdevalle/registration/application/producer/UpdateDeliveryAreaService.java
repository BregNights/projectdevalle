package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.UpdateDeliveryAreaCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDeliveryAreaService {

    private final ProducerRepository producerRepository;

    public UpdateDeliveryAreaService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Transactional
    public void update(UpdateDeliveryAreaCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));
        producer.updateDeliveryArea(command.deliveryArea());
        producerRepository.save(producer);
    }
}
