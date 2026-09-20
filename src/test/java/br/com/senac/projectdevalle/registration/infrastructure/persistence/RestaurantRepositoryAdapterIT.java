package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.Role;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Test
    void savesAndReloadsRestaurantWithDeliveryAddresses() {
        User user = User.register(new Email("maria@bomsabor.com"), "S3nhaForte!", Role.RESTAURANT, passwordHasher);
        UUID userId = userRepository.save(user).id();
        Restaurant restaurant = Restaurant.register(userId, "Restaurante Bom Sabor",
                new Cnpj("12345678000195"), EstablishmentCategory.BISTRO,
                new Contact("Maria", "Compras", "47999999999", "maria@bomsabor.com"));
        restaurant.addDeliveryAddress(DeliveryAddress.add("Matriz", validAddress(),
                new Coordinates(-26.9077, -48.6613), false));

        restaurantRepository.save(restaurant);

        Optional<Restaurant> reloaded = restaurantRepository.findById(restaurant.id());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().corporateName()).isEqualTo("Restaurante Bom Sabor");
        assertThat(reloaded.get().deliveryAddresses()).singleElement().matches(DeliveryAddress::primary);
    }

    private static Address validAddress() {
        return new Address("Rua XV de Novembro", "500", "Centro", "Itajai", "SC", "88301-000", null);
    }
}
