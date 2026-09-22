package br.com.senac.projectdevalle.ordering.infrastructure.registration;

import br.com.senac.projectdevalle.ordering.application.port.OrderingPartiesPort;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class OrderingPartiesAdapter implements OrderingPartiesPort {

    private final RestaurantRepository restaurantRepository;
    private final ProducerRepository producerRepository;

    OrderingPartiesAdapter(RestaurantRepository restaurantRepository, ProducerRepository producerRepository) {
        this.restaurantRepository = restaurantRepository;
        this.producerRepository = producerRepository;
    }

    @Override
    public Optional<UUID> restaurantIdByUserId(UUID userId) {
        return restaurantRepository.findByUserId(userId).map(Restaurant::id);
    }

    @Override
    public Optional<UUID> producerIdByUserId(UUID userId) {
        return producerRepository.findByUserId(userId).map(Producer::id);
    }

    @Override
    public boolean isRestaurantEligible(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId).map(Restaurant::isEligibleToOperate).orElse(false);
    }

    @Override
    public boolean isProducerEligible(UUID producerId) {
        return producerRepository.findById(producerId).map(Producer::isEligibleToOperate).orElse(false);
    }

    @Override
    public Optional<DeliveryDestination> deliveryAddress(UUID restaurantId, UUID deliveryAddressId) {
        return restaurantRepository.findById(restaurantId)
                .flatMap(restaurant -> restaurant.deliveryAddresses().stream()
                        .filter(address -> address.id().equals(deliveryAddressId))
                        .findFirst())
                .map(address -> new DeliveryDestination(address.id(), address.label(), address.address(),
                        address.coordinates()));
    }

    @Override
    public String restaurantName(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId).map(Restaurant::corporateName).orElse(null);
    }

    @Override
    public String producerName(UUID producerId) {
        return producerRepository.findById(producerId).map(Producer::name).orElse(null);
    }
}
