package br.com.senac.projectdevalle.registration.infrastructure.persistence.mapper;

import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.RestaurantDeliveryAddressJpaEntity;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.RestaurantJpaEntity;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Component;

import java.util.List;

// Mapeamento manual pelo mesmo motivo do ProducerEntityMapper: Restaurant é um agregado
// com construtor privado (reconstitute), não um bean simples de getters/setters.
@Component
public class RestaurantEntityMapper {

    public RestaurantJpaEntity toEntity(Restaurant restaurant) {
        Contact contact = restaurant.contact();

        RestaurantJpaEntity entity = RestaurantJpaEntity.builder()
                .id(restaurant.id())
                .userId(restaurant.userId())
                .corporateName(restaurant.corporateName())
                .cnpj(restaurant.cnpj().digits())
                .category(restaurant.category())
                .contactName(contact.name())
                .contactRole(contact.role())
                .contactPhone(contact.phone())
                .contactEmail(contact.email())
                .status(restaurant.status())
                .build();

        entity.setDeliveryAddresses(restaurant.deliveryAddresses().stream()
                .map(address -> RestaurantDeliveryAddressJpaEntity.builder()
                        .id(address.id())
                        .restaurant(entity)
                        .label(address.label())
                        .street(address.address().street())
                        .number(address.address().number())
                        .neighborhood(address.address().neighborhood())
                        .city(address.address().city())
                        .state(address.address().state())
                        .zipCode(address.address().zipCode())
                        .complement(address.address().complement())
                        .latitude(address.coordinates() != null ? address.coordinates().latitude() : null)
                        .longitude(address.coordinates() != null ? address.coordinates().longitude() : null)
                        .primary(address.primary())
                        .build())
                .toList());

        return entity;
    }

    public Restaurant toDomain(RestaurantJpaEntity entity) {
        Contact contact = new Contact(entity.getContactName(), entity.getContactRole(), entity.getContactPhone(),
                entity.getContactEmail());

        List<DeliveryAddress> deliveryAddresses = entity.getDeliveryAddresses().stream()
                .map(this::toDomainAddress)
                .toList();

        return Restaurant.reconstitute(entity.getId(), entity.getUserId(), entity.getCorporateName(),
                new Cnpj(entity.getCnpj()), entity.getCategory(), contact, deliveryAddresses, entity.getStatus());
    }

    private DeliveryAddress toDomainAddress(RestaurantDeliveryAddressJpaEntity entity) {
        Address address = new Address(entity.getStreet(), entity.getNumber(), entity.getNeighborhood(),
                entity.getCity(), entity.getState(), entity.getZipCode(), entity.getComplement());

        Coordinates coordinates = entity.getLatitude() != null && entity.getLongitude() != null
                ? new Coordinates(entity.getLatitude(), entity.getLongitude())
                : null;

        return new DeliveryAddress(entity.getId(), entity.getLabel(), address, coordinates, entity.isPrimary());
    }
}
