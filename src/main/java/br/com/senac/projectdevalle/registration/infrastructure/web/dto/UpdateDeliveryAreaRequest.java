package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.DeliveryArea;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateDeliveryAreaRequest(@NotEmpty List<String> municipalities) {

    public DeliveryArea toDomain() {
        return new DeliveryArea(municipalities);
    }
}
