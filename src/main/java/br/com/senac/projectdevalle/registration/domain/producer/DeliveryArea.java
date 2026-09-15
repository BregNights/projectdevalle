package br.com.senac.projectdevalle.registration.domain.producer;

import java.util.List;

public record DeliveryArea(List<String> municipalities) {

    public static final DeliveryArea NONE = new DeliveryArea(List.of());

    public DeliveryArea {
        municipalities = municipalities == null ? List.of() : List.copyOf(municipalities);
    }
}
</content>
