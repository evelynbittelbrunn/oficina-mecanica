package br.com.oficina.catalog.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;

import java.util.UUID;

public class ServiceOffering {

    private static final int MAX_NAME_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final UUID id;
    private String name;
    private String description;
    private Money basePrice;
    private int estimatedMinutes;
    private boolean active;

    public ServiceOffering(
            UUID id,
            String name,
            String description,
            Money basePrice,
            Integer estimatedMinutes,
            boolean active
    ) {
        if (id == null) {
            throw new ValidationException("ID do serviço não pode ser nulo.");
        }

        this.id = id;
        this.active = active;
        apply(name, description, basePrice, estimatedMinutes);
    }

    public static ServiceOffering create(String name, String description, Money basePrice, Integer estimatedMinutes) {
        return new ServiceOffering(UUID.randomUUID(), name, description, basePrice, estimatedMinutes, true);
    }

    public void update(String name, String description, Money basePrice, Integer estimatedMinutes) {
        apply(name, description, basePrice, estimatedMinutes);
    }

    public void activate() {
        this.active = true;
    }

    /** Services are never physically deleted: older service orders still reference them. */
    public void deactivate() {
        this.active = false;
    }

    private void apply(String name, String description, Money basePrice, Integer estimatedMinutes) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome do serviço não pode ser vazio.");
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Nome do serviço deve ter no máximo " + MAX_NAME_LENGTH + " caracteres.");
        }
        if (description != null && description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Descrição deve ter no máximo " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (basePrice == null) {
            throw new ValidationException("Preço base do serviço é obrigatório.");
        }
        if (estimatedMinutes == null || estimatedMinutes <= 0) {
            throw new ValidationException("Tempo estimado deve ser maior que zero.");
        }

        this.name = name.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.basePrice = basePrice;
        this.estimatedMinutes = estimatedMinutes;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public boolean isActive() {
        return active;
    }
}
