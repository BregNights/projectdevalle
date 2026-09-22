package br.com.senac.projectdevalle.ordering.domain.order;

import java.math.BigDecimal;
import java.time.Instant;

// RF20 — cancelamento com registro do motivo. afterConfirmation indica que o pedido já estava confirmado
// (base da taxa de cumprimento do produtor — RN11); penaltyAmount é a multa do restaurante (RN10).
public record Cancellation(OrderParty cancelledBy, String reason, Instant cancelledAt, boolean afterConfirmation,
                           BigDecimal penaltyAmount) {
}
