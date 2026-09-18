package br.com.senac.projectdevalle.registration.domain.restaurant;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {

    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(UUID id);

    Optional<Restaurant> findByUserId(UUID userId);

    List<Restaurant> findByStatus(RegistrationStatus status);
}
