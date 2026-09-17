package br.com.senac.projectdevalle.registration.application.restaurant;

import br.com.senac.projectdevalle.registration.application.restaurant.command.AddDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddDeliveryAddressService {

    private final RestaurantRepository restaurantRepository;
    private final GeocodingPort geocodingPort;

    public AddDeliveryAddressService(RestaurantRepository restaurantRepository, GeocodingPort geocodingPort) {
        this.restaurantRepository = restaurantRepository;
        this.geocodingPort = geocodingPort;
    }

    @Transactional
    public void add(AddDeliveryAddressCommand command) {
        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));

        Coordinates coordinates = geocodeBestEffort(command.address());
        restaurant.addDeliveryAddress(DeliveryAddress.add(command.label(), command.address(), coordinates,
                command.primary()));

        restaurantRepository.save(restaurant);
    }

    private Coordinates geocodeBestEffort(Address address) {
        try {
            return geocodingPort.geocode(address);
        } catch (GeolocationUnavailableException exception) {
            return null;
        }
    }
}
</content>
