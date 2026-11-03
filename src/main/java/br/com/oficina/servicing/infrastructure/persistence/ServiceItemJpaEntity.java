package br.com.oficina.servicing.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "service_order_services")
public class ServiceItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    protected ServiceItemJpaEntity() {
    }

    ServiceItemJpaEntity(UUID id, UUID serviceId, String name, BigDecimal unitPrice, Integer quantity) {
        this.id = id;
        this.serviceId = serviceId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    UUID getId() {
        return id;
    }

    UUID getServiceId() {
        return serviceId;
    }

    String getName() {
        return name;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }

    Integer getQuantity() {
        return quantity;
    }
}
