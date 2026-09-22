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

// RF16 — carrinho: itens de um ou mais produtores, endereço de entrega (RF30.2) e data desejada.
public record CheckoutRequest(
        @NotNull UUID deliveryAddressId,
        @NotNull LocalDate requestedDeliveryDate,
        @Size(max = 1000) String notes,
        @NotEmpty List<@Valid Item> items
) {

    public record Item(
            @NotNull UUID offerId,
            @NotNull @DecimalMin(value = "0.001") BigDecimal quantity,
            // RF18 — preço proposto pelo restaurante (opcional; sem ele vale o preço da oferta).
            @DecimalMin(value = "0.01") BigDecimal proposedUnitPrice
    ) {
    }
}
