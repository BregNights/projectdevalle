package br.com.senac.projectdevalle.registration.application.producer;

import java.util.UUID;

public record RegisterProducerResult(UUID producerId, UUID userId, boolean geocodingPending) {
}
</content>
