package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRegistrationRequest(@NotBlank @Size(max = 1000) String reason) {
}
