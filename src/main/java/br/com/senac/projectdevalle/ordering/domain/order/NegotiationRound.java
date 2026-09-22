package br.com.senac.projectdevalle.ordering.domain.order;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// RF18 — histórico da negociação: cada proposta/contraproposta com quem propôs, quando, os termos e a data de
// entrega proposta (nula = mantida).
public record NegotiationRound(int number, OrderParty proposedBy, Instant proposedAt, String message,
                               LocalDate deliveryDate, List<ItemTerms> terms) {

    public NegotiationRound {
        terms = List.copyOf(terms);
    }
}
