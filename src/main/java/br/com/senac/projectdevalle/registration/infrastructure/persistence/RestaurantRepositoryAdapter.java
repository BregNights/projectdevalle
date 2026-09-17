package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.mapper.RestaurantEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class RestaurantRepositoryAdapter implements RestaurantRepository {

    private final RestaurantJpaRepository jpaRepository;
    private final RestaurantEntityMapper mapper;

    RestaurantRepositoryAdapter(RestaurantJpaRepository jpaRepository, RestaurantEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantJpaEntity saved = jpaRepository.save(mapper.toEntity(restaurant));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Restaurant> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
</content>
