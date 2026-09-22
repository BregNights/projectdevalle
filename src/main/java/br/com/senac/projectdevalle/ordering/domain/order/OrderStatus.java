package br.com.senac.projectdevalle.ordering.domain.order;

// RF19 — status rastreável do pedido.
public enum OrderStatus {
    // Aguardando resposta de uma das partes (aceite, contraproposta ou recusa — RF18).
    PENDING,
    // Produtor aceitou quantidade, preço e prazo (RN09); o estoque da oferta foi baixado.
    CONFIRMED,
    // Produtor iniciou colheita/pesca/preparo (a partir daqui o cancelamento pelo restaurante gera multa — RN10).
    IN_PREPARATION,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED;

    // Pedido cujo estoque está reservado na oferta.
    public boolean holdsStock() {
        return this == CONFIRMED || this == IN_PREPARATION || this == IN_TRANSIT || this == DELIVERED;
    }
}
