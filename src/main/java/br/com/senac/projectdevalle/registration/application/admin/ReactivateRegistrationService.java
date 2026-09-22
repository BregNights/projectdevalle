package br.com.senac.projectdevalle.registration.application.admin;

import br.com.senac.projectdevalle.registration.application.admin.command.ReactivateProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.ReactivateRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF40 — administrador pode reativar um cadastro suspenso.
@Service
public class ReactivateRegistrationService {

    private final ProducerRepository producerRepository;
    private final RestaurantRepository restaurantRepository;
    private final CoverageAreaPolicy coverageAreaPolicy;

    public ReactivateRegistrationService(ProducerRepository producerRepository,
                                          RestaurantRepository restaurantRepository,
                                          CoverageAreaPolicy coverageAreaPolicy) {
        this.producerRepository = producerRepository;
        this.restaurantRepository = restaurantRepository;
        this.coverageAreaPolicy = coverageAreaPolicy;
    }

    @Transactional
    public void reactivateProducer(ReactivateProducerRegistrationCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));
        producer.reactivate(coverageAreaPolicy);
        producerRepository.save(producer);
    }

    @Transactional
    public void reactivateRestaurant(ReactivateRestaurantRegistrationCommand command) {
        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));
        restaurant.reactivate();
        restaurantRepository.save(restaurant);
    }
}
