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
    public DeliveryAddress add(AddDeliveryAddressCommand command) {
        Restaurant restaurant = restaurantRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for current user"));

        Coordinates coordinates = geocodeBestEffort(command.address());
        DeliveryAddress deliveryAddress = DeliveryAddress.add(command.label(), command.address(), coordinates,
                command.primary());
        restaurant.addDeliveryAddress(deliveryAddress);

        restaurantRepository.save(restaurant);

        return restaurant.deliveryAddresses().stream()
                .filter(address -> address.id().equals(deliveryAddress.id()))
                .findFirst()
                .orElseThrow();
    }

    private Coordinates geocodeBestEffort(Address address) {
        try {
            return geocodingPort.geocode(address);
        } catch (GeolocationUnavailableException exception) {
            return null;
        }
    }
}
