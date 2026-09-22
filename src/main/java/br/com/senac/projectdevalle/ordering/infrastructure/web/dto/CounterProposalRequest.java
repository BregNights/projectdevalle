package br.com.senac.projectdevalle.ordering.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// RF18 — contraproposta: novos termos por item e, opcionalmente, nova data de entrega.
public record CounterProposalRequest(
        @NotEmpty List<@Valid ItemTerms> items,
        LocalDate deliveryDate,
        @Size(max = 1000) String message
) {

    public record ItemTerms(
            @NotNull UUID itemId,
            @NotNull @DecimalMin(value = "0.001") BigDecimal quantity,
            @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice
    ) {
    }
}
