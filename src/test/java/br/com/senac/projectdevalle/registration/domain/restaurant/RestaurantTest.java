package br.com.senac.projectdevalle.registration.domain.restaurant;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestaurantTest {

    @Test
    void approveFailsWithoutAnyDeliveryAddress() {
        Restaurant restaurant = newRestaurant();

        assertThatThrownBy(restaurant::approve).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void firstDeliveryAddressBecomesPrimaryAutomatically() {
        Restaurant restaurant = newRestaurant();

        restaurant.addDeliveryAddress(DeliveryAddress.add("Matriz", validAddress(), null, false));

        assertThat(restaurant.deliveryAddresses()).singleElement().matches(DeliveryAddress::primary);
    }

    @Test
    void addingANewPrimaryAddressClearsThePreviousOne() {
        Restaurant restaurant = newRestaurant();
        restaurant.addDeliveryAddress(DeliveryAddress.add("Matriz", validAddress(), null, false));
        restaurant.addDeliveryAddress(DeliveryAddress.add("Filial", validAddress(), null, true));

        assertThat(restaurant.deliveryAddresses())
                .filteredOn(DeliveryAddress::primary)
                .hasSize(1)
                .extracting(DeliveryAddress::label)
                .containsExactly("Filial");
    }

    @Test
    void definePrimaryAddressSwitchesTheFlag() {
        Restaurant restaurant = newRestaurant();
        restaurant.addDeliveryAddress(DeliveryAddress.add("Matriz", validAddress(), null, false));
        restaurant.addDeliveryAddress(DeliveryAddress.add("Filial", validAddress(), null, false));
        UUID filialId = restaurant.deliveryAddresses().get(1).id();

        restaurant.definePrimaryAddress(filialId);

        assertThat(restaurant.deliveryAddresses())
                .filteredOn(DeliveryAddress::primary)
                .extracting(DeliveryAddress::id)
                .containsExactly(filialId);
    }

    @Test
    void approveSucceedsWithAtLeastOneAddress() {
        Restaurant restaurant = newRestaurant();
        restaurant.addDeliveryAddress(DeliveryAddress.add("Matriz", validAddress(), null, false));

        restaurant.approve();

        assertThat(restaurant.isEligibleToOperate()).isTrue();
    }

    private static Restaurant newRestaurant() {
        return Restaurant.register(UUID.randomUUID(), "Restaurante Bom Sabor", new Cnpj("12345678000195"),
                EstablishmentCategory.BISTRO, new Contact("Maria", "Compras", "47999999999", "maria@bomsabor.com"));
    }

    private static Address validAddress() {
        return new Address("Rua XV de Novembro", "500", "Centro", "Blumenau", "SC", "89010-000", null);
    }
}
