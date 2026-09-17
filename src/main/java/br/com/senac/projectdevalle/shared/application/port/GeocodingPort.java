package br.com.senac.projectdevalle.shared.application.port;

import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

// Porta de saída para conversão de endereço em coordenadas geográficas (RF30.1/RF30.2).
public interface GeocodingPort {

    // Lança GeolocationUnavailableException quando o provedor está indisponível ou não
    // encontra o endereço — nunca propaga exceptions de infraestrutura (HTTP, parsing) para o domínio.
    Coordinates geocode(Address address) throws GeolocationUnavailableException;
}
