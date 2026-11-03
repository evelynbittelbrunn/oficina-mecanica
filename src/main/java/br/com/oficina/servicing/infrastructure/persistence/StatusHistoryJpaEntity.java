package br.com.oficina.servicing.infrastructure.persistence;

import br.com.oficina.servicing.domain.ServiceOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_order_status_history")
public class StatusHistoryJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private ServiceOrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private ServiceOrderStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(nullable = false, length = 150)
    private String actor;

    @Column(length = 600)
    private String note;

    protected StatusHistoryJpaEntity() {
    }

    StatusHistoryJpaEntity(UUID id, ServiceOrderStatus previousStatus, ServiceOrderStatus newStatus,
                           Instant changedAt, String actor, String note) {
        this.id = id;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
        this.actor = actor;
        this.note = note;
    }

    UUID getId() {
        return id;
    }

    ServiceOrderStatus getPreviousStatus() {
        return previousStatus;
    }

    ServiceOrderStatus getNewStatus() {
        return newStatus;
    }

    Instant getChangedAt() {
        return changedAt;
    }

    String getActor() {
        return actor;
    }

    String getNote() {
        return note;
    }
}
