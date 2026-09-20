package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;

import java.util.List;
import java.util.UUID;

// Visão detalhada do próprio cadastro (/me/account) — inclui contato completo e a lista de
// endereços de entrega, que RestaurantResponse (público/admin) não expõe (só a contagem).
public record RestaurantAccountResponse(
        UUID id,
        String corporateName,
        String cnpj,
        EstablishmentCategory category,
        ContactResponse contact,
        List<DeliveryAddressResponse> deliveryAddresses
) {

    public static RestaurantAccountResponse from(Restaurant restaurant) {
        return new RestaurantAccountResponse(
                restaurant.id(),
                restaurant.corporateName(),
                restaurant.cnpj().digits(),
                restaurant.category(),
                ContactResponse.from(restaurant.contact()),
                restaurant.deliveryAddresses().stream().map(DeliveryAddressResponse::from).toList());
    }
}
