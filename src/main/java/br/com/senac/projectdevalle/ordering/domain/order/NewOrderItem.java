package br.com.senac.projectdevalle.ordering.domain.order;

import java.math.BigDecimal;
import java.util.UUID;

// Item no momento da compra: dados copiados da oferta (nome, categoria, unidade, preço de tabela) e, opcionalmente,
// um preço proposto pelo restaurante (RF18). Sem proposta, vale o preço de tabela.
public record NewOrderItem(UUID offerId, String productName, String category, String unit, BigDecimal listUnitPrice,
                           BigDecimal quantity, BigDecimal proposedUnitPrice) {
}
