package br.com.senac.projectdevalle.catalog.domain.offer;

import br.com.senac.projectdevalle.catalog.domain.offer.exception.InvalidOfferTransitionException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.MissingAvailabilityDeadlineException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Offer {

    private final UUID id;
    private final UUID producerId;
    private String productName;
    private final ProductCategory category;
    private final MeasurementUnit unit;
    private BigDecimal price;
    private BigDecimal quantityAvailable;
    private final Recurrence recurrence;
    private AvailabilityWindow availabilityWindow;
    private final List<String> photoUrls = new ArrayList<>();
    private OfferStatus status;

    private Offer(UUID id, UUID producerId, String productName, ProductCategory category, MeasurementUnit unit,
                  BigDecimal price, BigDecimal quantityAvailable, Recurrence recurrence,
                  AvailabilityWindow availabilityWindow, OfferStatus status) {
        this.id = id;
        this.producerId = producerId;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.recurrence = recurrence;
        this.availabilityWindow = availabilityWindow;
        this.status = status;
    }

    // RF07/RF08 — publicação de oferta pelo produtor aprovado (elegibilidade é verificada na camada de aplicação).
    public static Offer publish(UUID producerId, String productName, ProductCategory category, MeasurementUnit unit,
                                 BigDecimal price, BigDecimal quantityAvailable, Recurrence recurrence,
                                 AvailabilityWindow availabilityWindow, List<String> photoUrls) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName must not be blank");
        }
        // RN04 — quantidade disponível deve ser positiva para uma oferta nascer ativa.
        if (quantityAvailable == null || quantityAvailable.signum() <= 0) {
            throw new IllegalArgumentException("quantityAvailable must be greater than zero");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("price must be greater than zero");
        }
        // RN06 — categorias perecíveis exigem prazo de validade obrigatório.
        if (category.isPerishable() && (availabilityWindow == null || availabilityWindow.until() == null)) {
            throw new MissingAvailabilityDeadlineException(
                    "Perishable offers require an availability deadline (RN06)");
        }
        Offer offer = new Offer(UUID.randomUUID(), producerId, productName, category, unit, price,
                quantityAvailable, recurrence, availabilityWindow, OfferStatus.ACTIVE);
        if (photoUrls != null) {
            offer.photoUrls.addAll(photoUrls);
        }
        return offer;
    }

    public static Offer reconstitute(UUID id, UUID producerId, String productName, ProductCategory category,
                                      MeasurementUnit unit, BigDecimal price, BigDecimal quantityAvailable,
                                      Recurrence recurrence, AvailabilityWindow availabilityWindow,
                                      List<String> photoUrls, OfferStatus status) {
        Offer offer = new Offer(id, producerId, productName, category, unit, price, quantityAvailable, recurrence,
                availabilityWindow, status);
        if (photoUrls != null) {
            offer.photoUrls.addAll(photoUrls);
        }
        return offer;
    }

    public void pause() {
        requireStatusIn("pause", OfferStatus.ACTIVE);
        this.status = OfferStatus.PAUSED;
    }

    public void resume() {
        requireStatusIn("resume", OfferStatus.PAUSED, OfferStatus.SOLD_OUT);
        this.status = quantityAvailable.signum() > 0 ? OfferStatus.ACTIVE : OfferStatus.SOLD_OUT;
    }

    public void remove() {
        requireNotRemoved("remove");
        this.status = OfferStatus.REMOVED;
    }

    // productName/category/unit são imutáveis após a publicação: para vender algo diferente, publique nova oferta.
    public void updateDetails(BigDecimal newPrice, AvailabilityWindow newAvailabilityWindow,
                               List<String> newPhotoUrls) {
        requireNotRemoved("update");
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new IllegalArgumentException("price must be greater than zero");
        }
        if (category.isPerishable() && (newAvailabilityWindow == null || newAvailabilityWindow.until() == null)) {
            throw new MissingAvailabilityDeadlineException(
                    "Perishable offers require an availability deadline (RN06)");
        }
        this.price = newPrice;
        this.availabilityWindow = newAvailabilityWindow;
        this.photoUrls.clear();
        if (newPhotoUrls != null) {
            this.photoUrls.addAll(newPhotoUrls);
        }
    }

    // RN04 — controle de estoque: zera para SOLD_OUT, reabastecer reativa automaticamente.
    public void updateQuantity(BigDecimal newQuantity) {
        requireNotRemoved("update quantity");
        if (newQuantity == null || newQuantity.signum() < 0) {
            throw new IllegalArgumentException("quantityAvailable must not be negative");
        }
        this.quantityAvailable = newQuantity;
        if (newQuantity.signum() == 0) {
            if (status == OfferStatus.ACTIVE) {
                this.status = OfferStatus.SOLD_OUT;
            }
        } else if (status == OfferStatus.SOLD_OUT) {
            this.status = OfferStatus.ACTIVE;
        }
    }

    public boolean isExpired(Clock clock) {
        if (availabilityWindow == null || availabilityWindow.until() == null) {
            return false;
        }
        LocalDate today = LocalDate.now(clock);
        return today.isAfter(availabilityWindow.until());
    }

    public boolean isVisibleInCatalog(Clock clock) {
        return status == OfferStatus.ACTIVE && !isExpired(clock);
    }

    private void requireStatusIn(String action, OfferStatus... allowed) {
        for (OfferStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new InvalidOfferTransitionException("Cannot " + action + " an offer with status " + status);
    }

    private void requireNotRemoved(String action) {
        if (status == OfferStatus.REMOVED) {
            throw new InvalidOfferTransitionException("Cannot " + action + " a removed offer");
        }
    }

    public UUID id() {
        return id;
    }

    public UUID producerId() {
        return producerId;
    }

    public String productName() {
        return productName;
    }

    public ProductCategory category() {
        return category;
    }

    public MeasurementUnit unit() {
        return unit;
    }

    public BigDecimal price() {
        return price;
    }

    public BigDecimal quantityAvailable() {
        return quantityAvailable;
    }

    public Recurrence recurrence() {
        return recurrence;
    }

    public AvailabilityWindow availabilityWindow() {
        return availabilityWindow;
    }

    public List<String> photoUrls() {
        return List.copyOf(photoUrls);
    }

    public OfferStatus status() {
        return status;
    }
}
