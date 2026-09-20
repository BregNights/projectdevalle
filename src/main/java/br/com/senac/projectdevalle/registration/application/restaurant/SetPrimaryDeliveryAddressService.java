package br.com.senac.projectdevalle.registration.application.restaurant;

import br.com.senac.projectdevalle.registration.application.restaurant.command.SetPrimaryDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetPrimaryDeliveryAddressService {

    private final RestaurantRepository restaurantRepository;

    public SetPrimaryDeliveryAddressService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void setPrimary(SetPrimaryDeliveryAddressCommand command) {
        Restaurant restaurant = restaurantRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for current user"));
        restaurant.definePrimaryAddress(command.deliveryAddressId());
        restaurantRepository.save(restaurant);
    }
}
