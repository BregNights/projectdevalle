package br.com.senac.projectdevalle.registration.application.admin;

import br.com.senac.projectdevalle.registration.application.admin.command.SuspendProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.SuspendRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF40 — administrador pode suspender um cadastro já aprovado.
@Service
public class SuspendRegistrationService {

    private final ProducerRepository producerRepository;
    private final RestaurantRepository restaurantRepository;

    public SuspendRegistrationService(ProducerRepository producerRepository,
                                       RestaurantRepository restaurantRepository) {
        this.producerRepository = producerRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void suspendProducer(SuspendProducerRegistrationCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));
        producer.suspend(command.reason());
        producerRepository.save(producer);
    }

    @Transactional
    public void suspendRestaurant(SuspendRestaurantRegistrationCommand command) {
        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));
        restaurant.suspend(command.reason());
        restaurantRepository.save(restaurant);
    }
}
