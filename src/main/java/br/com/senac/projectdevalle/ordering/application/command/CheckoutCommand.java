package br.com.senac.projectdevalle.ordering.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// RF16 — carrinho com itens de um ou mais produtores. proposedUnitPrice é opcional (RF18).
public record CheckoutCommand(UUID userId, UUID deliveryAddressId, LocalDate requestedDeliveryDate, String notes,
                              List<CheckoutItem> items) {

    public record CheckoutItem(UUID offerId, BigDecimal quantity, BigDecimal proposedUnitPrice) {
    }
}
