package br.com.senac.projectdevalle.catalog.infrastructure.registration;

import br.com.senac.projectdevalle.catalog.application.port.RestaurantDirectoryPort;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RestaurantDirectoryAdapter implements RestaurantDirectoryPort {

    private final RestaurantRepository restaurantRepository;

    public RestaurantDirectoryAdapter(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Optional<UUID> findRestaurantIdByUserId(UUID userId) {
        return restaurantRepository.findByUserId(userId).map(Restaurant::id);
    }

    @Override
    public Optional<Coordinates> findPrimaryAddressCoordinates(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .flatMap(restaurant -> restaurant.deliveryAddresses().stream()
                        .filter(DeliveryAddress::primary)
                        .findFirst())
                .map(DeliveryAddress::coordinates);
    }
}
