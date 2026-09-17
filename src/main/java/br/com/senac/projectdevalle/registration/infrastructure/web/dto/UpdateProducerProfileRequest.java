package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProducerProfileRequest(@NotBlank String name) {
}
