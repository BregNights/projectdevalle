package br.com.senac.projectdevalle.registration.domain.producer;

import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

// Propriedade, barco/porto de desembarque ou ponto de coleta do produtor (RF30.1).
// coordinates é nulo quando a geocodificação ainda não foi resolvida (ver Producer.geocodingPending).
public record OriginLocation(Address address, Coordinates coordinates) {

    public OriginLocation {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
    }

    public static OriginLocation withoutCoordinates(Address address) {
        return new OriginLocation(address, null);
    }

    public OriginLocation withCoordinates(Coordinates newCoordinates) {
        return new OriginLocation(address, newCoordinates);
    }
}
</content>
