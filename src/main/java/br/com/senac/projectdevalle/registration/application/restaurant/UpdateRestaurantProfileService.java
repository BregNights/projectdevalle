package br.com.senac.projectdevalle.registration.application.restaurant;

import br.com.senac.projectdevalle.registration.application.restaurant.command.UpdateRestaurantProfileCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateRestaurantProfileService {

    private final RestaurantRepository restaurantRepository;

    public UpdateRestaurantProfileService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void update(UpdateRestaurantProfileCommand command) {
        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));
        restaurant.updateProfile(command.corporateName(), command.category(), command.contact());
        restaurantRepository.save(restaurant);
    }
}
</content>
