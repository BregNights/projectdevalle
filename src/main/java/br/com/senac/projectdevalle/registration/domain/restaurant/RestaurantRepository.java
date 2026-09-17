package br.com.senac.projectdevalle.registration.domain.restaurant;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {

    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(UUID id);
}
