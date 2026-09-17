package br.com.senac.projectdevalle.registration.application.admin;

import br.com.senac.projectdevalle.registration.application.admin.command.RejectProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RejectRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectRegistrationService {

    private final ProducerRepository producerRepository;
    private final RestaurantRepository restaurantRepository;

    public RejectRegistrationService(ProducerRepository producerRepository,
                                      RestaurantRepository restaurantRepository) {
        this.producerRepository = producerRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void rejectProducer(RejectProducerRegistrationCommand command) {
        Producer producer = producerRepository.findById(command.producerId())
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + command.producerId()));
        producer.reject(command.reason());
        producerRepository.save(producer);
    }

    @Transactional
    public void rejectRestaurant(RejectRestaurantRegistrationCommand command) {
        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));
        restaurant.reject(command.reason());
        restaurantRepository.save(restaurant);
    }
}
