package br.com.senac.projectdevalle.shared.application.port;

import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

// Porta de saída para cálculo de distância/tempo entre dois pontos (RF11/RF30.3).
// Ainda sem consumidor nesta fase (será usada pelos contextos catalog/logistics),
// mas o contrato já é fixado junto com GeocodingPort para não ser redesenhado depois.
public interface DistanceCalculationPort {

    EstimatedDistance calculate(Coordinates origin, Coordinates destination) throws GeolocationUnavailableException;
}
