package br.com.senac.projectdevalle.ordering.application.port;

import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;

import java.util.Optional;
import java.util.UUID;

// Integração com o Cadastro: quem é o restaurante/produtor do usuário logado, se está apto a operar (RN01)
// e os endereços de entrega do restaurante (RF30.2).
public interface OrderingPartiesPort {

    Optional<UUID> restaurantIdByUserId(UUID userId);

    Optional<UUID> producerIdByUserId(UUID userId);

    boolean isRestaurantEligible(UUID restaurantId);

    boolean isProducerEligible(UUID producerId);

    Optional<DeliveryDestination> deliveryAddress(UUID restaurantId, UUID deliveryAddressId);

    String restaurantName(UUID restaurantId);

    String producerName(UUID producerId);
}
