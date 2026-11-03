package br.com.oficina.registration.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "license_plate", nullable = false, unique = true, length = 7)
    private String licensePlate;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer year;

    protected VehicleJpaEntity() {
    }

    public VehicleJpaEntity(UUID id, UUID customerId, String licensePlate, String brand, String model, Integer year) {
        this.id = id;
        this.customerId = customerId;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public Integer getYear() {
        return year;
    }
}
