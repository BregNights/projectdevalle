package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterProducerRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String name,
        @NotNull TaxDocumentType taxDocumentType,
        @NotBlank String taxDocumentNumber,
        @NotNull ProductionType productionType,
        @NotNull @Valid AddressRequest originAddress,
        @NotEmpty List<@Valid SupportingDocumentRequest> supportingDocuments
) {
}
</content>
