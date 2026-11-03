package br.com.oficina.servicing.infrastructure.persistence;

import br.com.oficina.servicing.domain.QuoteStatus;
import br.com.oficina.servicing.domain.ServiceOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_orders")
public class ServiceOrderJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String number;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceOrderStatus status;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "diagnosis_notes", length = 2000)
    private String diagnosisNotes;

    @Column(name = "quote_total_amount", precision = 12, scale = 2)
    private BigDecimal quoteTotalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_status", length = 20)
    private QuoteStatus quoteStatus;

    @Column(name = "quote_sent_at")
    private Instant quoteSentAt;

    @Column(name = "quote_decided_at")
    private Instant quoteDecidedAt;

    @Column(name = "quote_rejection_reason", length = 500)
    private String quoteRejectionReason;

    @Version
    private Long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_order_id", nullable = false)
    @OrderColumn(name = "position")
    private List<ServiceItemJpaEntity> serviceItems = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_order_id", nullable = false)
    @OrderColumn(name = "position")
    private List<PartItemJpaEntity> partItems = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_order_id", nullable = false)
    @OrderColumn(name = "position")
    private List<StatusHistoryJpaEntity> history = new ArrayList<>();

    protected ServiceOrderJpaEntity() {
    }

    ServiceOrderJpaEntity(UUID id, String number, UUID customerId, UUID vehicleId, ServiceOrderStatus status,
                          Instant openedAt, String diagnosisNotes, Long version) {
        this.id = id;
        this.number = number;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.status = status;
        this.openedAt = openedAt;
        this.diagnosisNotes = diagnosisNotes;
        this.version = version;
    }

    void setQuote(BigDecimal totalAmount, QuoteStatus status, Instant sentAt, Instant decidedAt, String rejectionReason) {
        this.quoteTotalAmount = totalAmount;
        this.quoteStatus = status;
        this.quoteSentAt = sentAt;
        this.quoteDecidedAt = decidedAt;
        this.quoteRejectionReason = rejectionReason;
    }

    UUID getId() {
        return id;
    }

    String getNumber() {
        return number;
    }

    UUID getCustomerId() {
        return customerId;
    }

    UUID getVehicleId() {
        return vehicleId;
    }

    ServiceOrderStatus getStatus() {
        return status;
    }

    Instant getOpenedAt() {
        return openedAt;
    }

    String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    BigDecimal getQuoteTotalAmount() {
        return quoteTotalAmount;
    }

    QuoteStatus getQuoteStatus() {
        return quoteStatus;
    }

    Instant getQuoteSentAt() {
        return quoteSentAt;
    }

    Instant getQuoteDecidedAt() {
        return quoteDecidedAt;
    }

    String getQuoteRejectionReason() {
        return quoteRejectionReason;
    }

    Long getVersion() {
        return version;
    }

    List<ServiceItemJpaEntity> getServiceItems() {
        return serviceItems;
    }

    List<PartItemJpaEntity> getPartItems() {
        return partItems;
    }

    List<StatusHistoryJpaEntity> getHistory() {
        return history;
    }
}
