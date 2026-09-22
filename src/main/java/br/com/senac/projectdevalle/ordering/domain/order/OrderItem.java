package br.com.senac.projectdevalle.ordering.domain.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

// Item do pedido. Nome, categoria, unidade e preço de tabela são cópias da oferta no momento da compra;
// quantidade e preço unitário são os termos vigentes da negociação (RF18).
public final class OrderItem {

    private final UUID id;
    private final UUID offerId;
    private final String productName;
    private final String category;
    private final String unit;
    private final BigDecimal listUnitPrice;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private LocalDate harvestDate;
    private String lot;

    private OrderItem(UUID id, UUID offerId, String productName, String category, String unit,
                      BigDecimal listUnitPrice, BigDecimal quantity, BigDecimal unitPrice, LocalDate harvestDate,
                      String lot) {
        this.id = id;
        this.offerId = offerId;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.listUnitPrice = listUnitPrice;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.harvestDate = harvestDate;
        this.lot = lot;
    }

    static OrderItem from(NewOrderItem item) {
        if (item.offerId() == null) {
            throw new IllegalArgumentException("offerId is required");
        }
        if (item.quantity() == null || item.quantity().signum() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (item.listUnitPrice() == null || item.listUnitPrice().signum() <= 0) {
            throw new IllegalArgumentException("listUnitPrice must be greater than zero");
        }
        BigDecimal unitPrice = item.proposedUnitPrice() != null ? item.proposedUnitPrice() : item.listUnitPrice();
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("unitPrice must be greater than zero");
        }
        return new OrderItem(UUID.randomUUID(), item.offerId(), item.productName(), item.category(), item.unit(),
                item.listUnitPrice(), item.quantity(), unitPrice, null, null);
    }

    public static OrderItem reconstitute(UUID id, UUID offerId, String productName, String category, String unit,
                                         BigDecimal listUnitPrice, BigDecimal quantity, BigDecimal unitPrice,
                                         LocalDate harvestDate, String lot) {
        return new OrderItem(id, offerId, productName, category, unit, listUnitPrice, quantity, unitPrice,
                harvestDate, lot);
    }

    void applyTerms(ItemTerms terms) {
        this.quantity = terms.quantity();
        this.unitPrice = terms.unitPrice();
    }

    void recordTraceability(LocalDate harvestDate, String lot) {
        this.harvestDate = harvestDate;
        this.lot = lot == null || lot.isBlank() ? null : lot.trim();
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    public UUID id() {
        return id;
    }

    public UUID offerId() {
        return offerId;
    }

    public String productName() {
        return productName;
    }

    public String category() {
        return category;
    }

    public String unit() {
        return unit;
    }

    public BigDecimal listUnitPrice() {
        return listUnitPrice;
    }

    public BigDecimal quantity() {
        return quantity;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public LocalDate harvestDate() {
        return harvestDate;
    }

    public String lot() {
        return lot;
    }
}
