package br.com.senac.projectdevalle.registration.application.restaurant;

import br.com.senac.projectdevalle.registration.application.restaurant.command.SetPrimaryDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetPrimaryDeliveryAddressServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private SetPrimaryDeliveryAddressService service;

    @BeforeEach
    void setUp() {
        service = new SetPrimaryDeliveryAddressService(restaurantRepository);
    }

    @Test
    void switchesPrimaryAddressAndPersists() {
        UUID userId = UUID.randomUUID();
        Restaurant restaurant = aRestaurant(userId);
        restaurant.addDeliveryAddress(DeliveryAddress.add("Matriz", validAddress(), null, false));
        restaurant.addDeliveryAddress(DeliveryAddress.add("Filial", validAddress(), null, false));
        UUID filialId = restaurant.deliveryAddresses().get(1).id();
        when(restaurantRepository.findByUserId(userId)).thenReturn(Optional.of(restaurant));

        service.setPrimary(new SetPrimaryDeliveryAddressCommand(userId, filialId));

        assertThat(restaurant.deliveryAddresses()).filteredOn(DeliveryAddress::primary)
                .extracting(DeliveryAddress::id).containsExactly(filialId);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void rejectsWhenNoRestaurantIsRegisteredForCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(restaurantRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setPrimary(new SetPrimaryDeliveryAddressCommand(userId, UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Restaurant aRestaurant(UUID userId) {
        return Restaurant.register(userId, "Restaurante Bom Sabor", new Cnpj("12345678000195"),
                EstablishmentCategory.BISTRO, new Contact("Maria", "Compras", "47999999999", "maria@bomsabor.com"));
    }

    private static Address validAddress() {
        return new Address("Rua XV de Novembro", "500", "Centro", "Blumenau", "SC", "89010-000", null);
    }
}
