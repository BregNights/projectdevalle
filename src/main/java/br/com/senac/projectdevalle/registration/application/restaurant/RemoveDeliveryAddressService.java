package br.com.senac.projectdevalle.registration.application.restaurant;

import br.com.senac.projectdevalle.registration.application.restaurant.command.RemoveDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveDeliveryAddressService {

    private final RestaurantRepository restaurantRepository;

    public RemoveDeliveryAddressService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void remove(RemoveDeliveryAddressCommand command) {
        Restaurant restaurant = restaurantRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for current user"));
        restaurant.removeDeliveryAddress(command.deliveryAddressId());
        restaurantRepository.save(restaurant);
    }
}
