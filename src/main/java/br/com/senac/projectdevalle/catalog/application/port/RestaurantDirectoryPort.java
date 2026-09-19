package br.com.senac.projectdevalle.catalog.application.port;

import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantDirectoryPort {

    Optional<UUID> findRestaurantIdByUserId(UUID userId);

    // RF11 — usado para calcular a distância até cada oferta do catálogo. Vazio se não houver
    // endereço principal com coordenadas resolvidas.
    Optional<Coordinates> findPrimaryAddressCoordinates(UUID restaurantId);
}
