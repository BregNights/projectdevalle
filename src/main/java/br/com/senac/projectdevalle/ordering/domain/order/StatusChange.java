package br.com.senac.projectdevalle.ordering.domain.order;

import java.time.Instant;

// RF19 — linha do tempo do pedido.
public record StatusChange(OrderStatus status, OrderParty actor, Instant occurredAt, String note) {
}
