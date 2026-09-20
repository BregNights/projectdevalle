package br.com.senac.projectdevalle.registration.domain.restaurant;

import br.com.senac.projectdevalle.registration.domain.common.Registrable;
import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Restaurant implements Registrable {

    private final UUID id;
    private final UUID userId;
    private String corporateName;
    private final Cnpj cnpj;
    private EstablishmentCategory category;
    private Contact contact;
    private final List<DeliveryAddress> deliveryAddresses = new ArrayList<>();
    private RegistrationStatus status;

    private Restaurant(UUID id, UUID userId, String corporateName, Cnpj cnpj, EstablishmentCategory category,
                        Contact contact, RegistrationStatus status) {
        this.id = id;
        this.userId = userId;
        this.corporateName = corporateName;
        this.cnpj = cnpj;
        this.category = category;
        this.contact = contact;
        this.status = status;
    }

    public static Restaurant register(UUID userId, String corporateName, Cnpj cnpj, EstablishmentCategory category,
                                       Contact contact) {
        if (corporateName == null || corporateName.isBlank()) {
            throw new IllegalArgumentException("corporateName must not be blank");
        }
        return new Restaurant(UUID.randomUUID(), userId, corporateName, cnpj, category, contact,
                RegistrationStatus.PENDING);
    }

    public static Restaurant reconstitute(UUID id, UUID userId, String corporateName, Cnpj cnpj,
                                           EstablishmentCategory category, Contact contact,
                                           List<DeliveryAddress> deliveryAddresses, RegistrationStatus status) {
        Restaurant restaurant = new Restaurant(id, userId, corporateName, cnpj, category, contact, status);
        restaurant.deliveryAddresses.addAll(deliveryAddresses);
        return restaurant;
    }

    // RF30.2 — restaurante deve poder cadastrar mais de um endereço de entrega.
    public void addDeliveryAddress(DeliveryAddress deliveryAddress) {
        boolean shouldBePrimary = deliveryAddresses.isEmpty() || deliveryAddress.primary();
        DeliveryAddress toAdd = deliveryAddress.withPrimary(shouldBePrimary);
        if (shouldBePrimary) {
            clearCurrentPrimary();
        }
        deliveryAddresses.add(toAdd);
    }

    public void definePrimaryAddress(UUID deliveryAddressId) {
        boolean exists = deliveryAddresses.stream().anyMatch(address -> address.id().equals(deliveryAddressId));
        if (!exists) {
            throw new BusinessRuleViolationException("Delivery address not found: " + deliveryAddressId);
        }
        clearCurrentPrimary();
        deliveryAddresses.replaceAll(address -> address.id().equals(deliveryAddressId)
                ? address.withPrimary(true)
                : address);
    }

    private void clearCurrentPrimary() {
        deliveryAddresses.replaceAll(address -> address.withPrimary(false));
    }

    // RF05 — remoção de endereço de entrega; sempre deve sobrar ao menos um (RF30.2/aprovação exige isso),
    // e se o removido era o principal, o próximo da lista assume automaticamente.
    public void removeDeliveryAddress(UUID deliveryAddressId) {
        DeliveryAddress toRemove = deliveryAddresses.stream()
                .filter(address -> address.id().equals(deliveryAddressId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Delivery address not found: " + deliveryAddressId));
        if (deliveryAddresses.size() == 1) {
            throw new BusinessRuleViolationException("At least one delivery address must remain");
        }
        deliveryAddresses.remove(toRemove);
        if (toRemove.primary()) {
            deliveryAddresses.set(0, deliveryAddresses.get(0).withPrimary(true));
        }
    }

    public void approve() {
        if (status != RegistrationStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending registrations can be approved");
        }
        if (deliveryAddresses.isEmpty()) {
            throw new BusinessRuleViolationException("At least one delivery address is required to approve");
        }
        this.status = RegistrationStatus.APPROVED;
    }

    @Override
    public void reject(String reason) {
        if (status != RegistrationStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending registrations can be rejected");
        }
        this.status = RegistrationStatus.REJECTED;
    }

    @Override
    public void suspend(String reason) {
        if (status != RegistrationStatus.APPROVED) {
            throw new BusinessRuleViolationException("Only approved registrations can be suspended");
        }
        this.status = RegistrationStatus.SUSPENDED;
    }

    @Override
    public boolean isEligibleToOperate() {
        return status == RegistrationStatus.APPROVED;
    }

    @Override
    public RegistrationStatus status() {
        return status;
    }

    public void updateProfile(String newCorporateName, EstablishmentCategory newCategory, Contact newContact) {
        if (newCorporateName == null || newCorporateName.isBlank()) {
            throw new IllegalArgumentException("corporateName must not be blank");
        }
        this.corporateName = newCorporateName;
        this.category = newCategory;
        this.contact = newContact;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String corporateName() {
        return corporateName;
    }

    public Cnpj cnpj() {
        return cnpj;
    }

    public EstablishmentCategory category() {
        return category;
    }

    public Contact contact() {
        return contact;
    }

    public List<DeliveryAddress> deliveryAddresses() {
        return List.copyOf(deliveryAddresses);
    }
}
