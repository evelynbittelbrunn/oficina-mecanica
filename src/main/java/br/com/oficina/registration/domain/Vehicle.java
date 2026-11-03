package br.com.oficina.registration.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.LicensePlate;

import java.time.Year;
import java.util.UUID;

public class Vehicle {

    public static final int MIN_YEAR = 1900;
    private static final int MAX_TEXT_LENGTH = 100;

    private final UUID id;
    private final UUID customerId;
    private final LicensePlate licensePlate;
    private String brand;
    private String model;
    private int year;

    public Vehicle(UUID id, UUID customerId, LicensePlate licensePlate, String brand, String model, Integer year) {
        if (id == null) {
            throw new ValidationException("ID do veículo não pode ser nulo.");
        }
        if (customerId == null) {
            throw new ValidationException("ID do cliente não pode ser nulo.");
        }
        if (licensePlate == null) {
            throw new ValidationException("Placa não pode ser nula.");
        }

        this.id = id;
        this.customerId = customerId;
        this.licensePlate = licensePlate;
        apply(brand, model, year);
    }

    public static Vehicle create(UUID customerId, LicensePlate licensePlate, String brand, String model, Integer year) {
        return new Vehicle(UUID.randomUUID(), customerId, licensePlate, brand, model, year);
    }

    public void update(String brand, String model, Integer year) {
        apply(brand, model, year);
    }

    public boolean belongsTo(UUID customerId) {
        return this.customerId.equals(customerId);
    }

    /** Highest accepted year: next year (next-year models). */
    public static int maxYear() {
        return Year.now().getValue() + 1;
    }

    private void apply(String brand, String model, Integer year) {
        this.brand = requiredText(brand, "Marca");
        this.model = requiredText(model, "Modelo");

        if (year == null || year < MIN_YEAR || year > maxYear()) {
            throw new ValidationException("Ano do veículo deve estar entre " + MIN_YEAR + " e " + maxYear() + ".");
        }

        this.year = year;
    }

    private static String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " não pode ser vazio.");
        }
        if (value.trim().length() > MAX_TEXT_LENGTH) {
            throw new ValidationException(field + " deve ter no máximo " + MAX_TEXT_LENGTH + " caracteres.");
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public LicensePlate getLicensePlate() {
        return licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }
}
