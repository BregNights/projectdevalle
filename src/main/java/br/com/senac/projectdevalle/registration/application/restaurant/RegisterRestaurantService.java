package br.com.senac.projectdevalle.registration.application.restaurant;

import br.com.senac.projectdevalle.registration.application.restaurant.command.RegisterRestaurantCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.Role;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterRestaurantService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordHasher passwordHasher;
    private final GeocodingPort geocodingPort;

    public RegisterRestaurantService(UserRepository userRepository, RestaurantRepository restaurantRepository,
                                      PasswordHasher passwordHasher, GeocodingPort geocodingPort) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordHasher = passwordHasher;
        this.geocodingPort = geocodingPort;
    }

    @Transactional
    public RegisterRestaurantResult register(RegisterRestaurantCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessRuleViolationException("Email already registered: " + command.email().value());
        }

        User user = User.register(command.email(), command.rawPassword(), Role.RESTAURANT, passwordHasher);
        userRepository.save(user);

        Restaurant restaurant = Restaurant.register(user.id(), command.corporateName(), command.cnpj(),
                command.category(), command.contact());

        Coordinates coordinates = geocodeBestEffort(command);
        restaurant.addDeliveryAddress(
                DeliveryAddress.add(command.initialAddressLabel(), command.initialAddress(), coordinates, true));

        restaurantRepository.save(restaurant);

        return new RegisterRestaurantResult(restaurant.id(), user.id());
    }

    private Coordinates geocodeBestEffort(RegisterRestaurantCommand command) {
        try {
            return geocodingPort.geocode(command.initialAddress());
        } catch (GeolocationUnavailableException exception) {
            return null;
        }
    }
}
