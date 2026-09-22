package br.com.senac.projectdevalle.ordering.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

// Integração com o Catálogo: dados da oferta no momento da compra e controle de estoque (RN04).
public interface OrderingCatalogPort {

    Optional<OfferSnapshot> findOffer(UUID offerId);

    // Baixa o estoque com lock no banco; falha se não houver quantidade suficiente.
    void reserveStock(UUID offerId, BigDecimal quantity);

    void releaseStock(UUID offerId, BigDecimal quantity);

    // purchasable = a oferta aparece no catálogo agora (ativa, dentro da validade, produtor apto e na área
    // atendida, categoria habilitada — RN01/RN02/RN06/RF43).
    record OfferSnapshot(UUID offerId, UUID producerId, String productName, String category, String unit,
                         BigDecimal price, BigDecimal quantityAvailable, LocalDate availableFrom,
                         LocalDate availableUntil, boolean purchasable) {
    }
}
