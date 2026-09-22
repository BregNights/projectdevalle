package br.com.senac.projectdevalle.registration.application.admin;

import br.com.senac.projectdevalle.registration.application.admin.command.RemoveProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RemoveRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// RF40 — remoção definitiva de um cadastro: o registro é mantido (histórico/auditoria) com status REMOVED
// e o usuário vinculado é desativado, perdendo o acesso à plataforma.
@Service
public class RemoveRegistrationService {

    private final ProducerRepository producerRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RemoveRegistrationService(ProducerRepository producerRepository,
                                      RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.producerRepository = producerRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void removeProducer(RemoveProducerRegistrationCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));
        producer.remove(command.reason());
        producerRepository.save(producer);
        deactivateUser(producer.userId());
    }

    @Transactional
    public void removeRestaurant(RemoveRestaurantRegistrationCommand command) {
        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));
        restaurant.remove(command.reason());
        restaurantRepository.save(restaurant);
        deactivateUser(restaurant.userId());
    }

    private void deactivateUser(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.deactivate();
            userRepository.save(user);
        });
    }
}
