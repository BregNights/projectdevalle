package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import java.util.UUID;

public record ProducerRegistrationResponse(UUID producerId, UUID userId, boolean geocodingPending) {
}
</content>
