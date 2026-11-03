package br.com.oficina.servicing.domain;

import br.com.oficina.servicing.domain.event.DiagnosisStarted;
import br.com.oficina.servicing.domain.event.ExecutionFinished;
import br.com.oficina.servicing.domain.event.ExecutionStarted;
import br.com.oficina.servicing.domain.event.QuoteApproved;
import br.com.oficina.servicing.domain.event.QuoteRejected;
import br.com.oficina.servicing.domain.event.QuoteSent;
import br.com.oficina.servicing.domain.event.ServiceOrderOpened;
import br.com.oficina.servicing.domain.event.VehicleDelivered;
import br.com.oficina.shared.domain.AggregateRoot;
import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ServiceOrder extends AggregateRoot {

    private static final int MAX_NOTES_LENGTH = 2000;
    private static final int MAX_REASON_LENGTH = 500;

    private final UUID id;
    private final ServiceOrderNumber number;
    private final UUID customerId;
    private final UUID vehicleId;
    private final Instant openedAt;
    private final List<ServiceItem> serviceItems;
    private final List<PartItem> partItems;
    private final List<StatusHistoryEntry> history;
    private final Long version;
    private ServiceOrderStatus status;
    private Quote quote;
    private String diagnosisNotes;

    private ServiceOrder(
            UUID id,
            ServiceOrderNumber number,
            UUID customerId,
            UUID vehicleId,
            Instant openedAt,
            ServiceOrderStatus status,
            List<ServiceItem> serviceItems,
            List<PartItem> partItems,
            Quote quote,
            String diagnosisNotes,
            List<StatusHistoryEntry> history,
            Long version
    ) {
        if (id == null || number == null || customerId == null || vehicleId == null || openedAt == null || status == null) {
            throw new ValidationException("Ordem de serviço deve ter id, número, cliente, veículo, data de abertura e status.");
        }

        this.id = id;
        this.number = number;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.openedAt = openedAt;
        this.status = status;
        this.serviceItems = new ArrayList<>(serviceItems);
        this.partItems = new ArrayList<>(partItems);
        this.quote = quote;
        this.diagnosisNotes = diagnosisNotes;
        this.history = new ArrayList<>(history);
        this.version = version;
    }

    public static ServiceOrder open(ServiceOrderNumber number, UUID customerId, UUID vehicleId, String actor) {
        Instant now = Instant.now();
        ServiceOrder order = new ServiceOrder(UUID.randomUUID(), number, customerId, vehicleId, now,
                ServiceOrderStatus.RECEIVED, List.of(), List.of(), null, null, List.of(), null);

        order.history.add(StatusHistoryEntry.record(null, ServiceOrderStatus.RECEIVED, actor, now, "Ordem de serviço aberta"));
        order.registerEvent(new ServiceOrderOpened(order.id, number.value(), customerId, vehicleId, now));
        return order;
    }

    public static ServiceOrder reconstitute(
            UUID id,
            ServiceOrderNumber number,
            UUID customerId,
            UUID vehicleId,
            Instant openedAt,
            ServiceOrderStatus status,
            List<ServiceItem> serviceItems,
            List<PartItem> partItems,
            Quote quote,
            String diagnosisNotes,
            List<StatusHistoryEntry> history,
            Long version
    ) {
        return new ServiceOrder(id, number, customerId, vehicleId, openedAt, status, serviceItems, partItems,
                quote, diagnosisNotes, history, version);
    }

    public void addService(UUID serviceId, String name, Money unitPrice, int quantity) {
        requireItemChangesAllowed();
        ServiceOrderItem.validateQuantity(quantity);

        serviceItems.stream()
                .filter(item -> item.refersTo(serviceId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.increase(quantity),
                        () -> serviceItems.add(ServiceItem.create(serviceId, name, unitPrice, quantity)));
    }

    public void removeService(UUID serviceId) {
        requireItemChangesAllowed();

        if (!serviceItems.removeIf(item -> item.refersTo(serviceId))) {
            throw new NotFoundException("Serviço não consta nesta ordem de serviço.");
        }
    }

    public void addPart(UUID inventoryItemId, String name, Money unitPrice, int quantity) {
        requireItemChangesAllowed();
        ServiceOrderItem.validateQuantity(quantity);

        partItems.stream()
                .filter(item -> item.refersTo(inventoryItemId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.increase(quantity),
                        () -> partItems.add(PartItem.create(inventoryItemId, name, unitPrice, quantity)));
    }

    public void removePart(UUID inventoryItemId) {
        requireItemChangesAllowed();

        if (!partItems.removeIf(item -> item.refersTo(inventoryItemId))) {
            throw new NotFoundException("Peça não consta nesta ordem de serviço.");
        }
    }

    public Money calculateTotal() {
        Money services = serviceItems.stream().map(ServiceOrderItem::subtotal).reduce(Money.ZERO, Money::add);
        Money parts = partItems.stream().map(ServiceOrderItem::subtotal).reduce(Money.ZERO, Money::add);
        return services.add(parts);
    }

    public void startDiagnosis(String actor) {
        requireStatus(ServiceOrderStatus.RECEIVED, "iniciar o diagnóstico");

        Instant now = transitionTo(ServiceOrderStatus.IN_DIAGNOSIS, actor, null);
        registerEvent(new DiagnosisStarted(id, number.value(), now));
    }

    public void completeDiagnosis(String notes, String actor) {
        requireStatus(ServiceOrderStatus.IN_DIAGNOSIS, "concluir o diagnóstico");

        if (serviceItems.isEmpty()) {
            throw new BusinessRuleException("Inclua ao menos um serviço antes de concluir o diagnóstico.");
        }
        if (notes != null && notes.trim().length() > MAX_NOTES_LENGTH) {
            throw new ValidationException("Observações devem ter no máximo " + MAX_NOTES_LENGTH + " caracteres.");
        }

        this.diagnosisNotes = notes == null || notes.isBlank() ? null : notes.trim();
        Money total = calculateTotal();

        Instant now = transitionTo(ServiceOrderStatus.AWAITING_APPROVAL, actor, "Orçamento enviado ao cliente");
        this.quote = Quote.generate(total, now);
        registerEvent(new QuoteSent(id, number.value(), customerId, total.amount(), now));
    }

    public void approveQuote(String actor) {
        requireStatus(ServiceOrderStatus.AWAITING_APPROVAL, "aprovar o orçamento");

        Instant now = transitionTo(ServiceOrderStatus.IN_PROGRESS, actor, "Orçamento aprovado pelo cliente");
        this.quote = quote.approve(now);
        registerEvent(new QuoteApproved(id, number.value(), quote.totalAmount().amount(), now));
        registerEvent(new ExecutionStarted(id, number.value(), now));
    }

    public void rejectQuote(String reason, String actor) {
        requireStatus(ServiceOrderStatus.AWAITING_APPROVAL, "rejeitar o orçamento");

        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Informe o motivo da rejeição do orçamento.");
        }
        if (reason.trim().length() > MAX_REASON_LENGTH) {
            throw new ValidationException("Motivo deve ter no máximo " + MAX_REASON_LENGTH + " caracteres.");
        }

        String normalizedReason = reason.trim();
        Instant now = transitionTo(ServiceOrderStatus.FINISHED, actor, "Orçamento rejeitado: " + normalizedReason);
        this.quote = quote.reject(normalizedReason, now);
        registerEvent(new QuoteRejected(id, number.value(), normalizedReason, now));
    }

    public void finishExecution(String actor) {
        requireStatus(ServiceOrderStatus.IN_PROGRESS, "finalizar a execução");

        Instant now = transitionTo(ServiceOrderStatus.FINISHED, actor, null);
        registerEvent(new ExecutionFinished(id, number.value(), now));
    }

    public void deliver(String actor) {
        requireStatus(ServiceOrderStatus.FINISHED, "entregar o veículo");

        Instant now = transitionTo(ServiceOrderStatus.DELIVERED, actor, null);
        registerEvent(new VehicleDelivered(id, number.value(), vehicleId, now));
    }

    private void requireStatus(ServiceOrderStatus expected, String operation) {
        if (status != expected) {
            throw new InvalidTransitionException(
                    "Não é possível " + operation + ": a OS está em " + status + " (esperado " + expected + ").");
        }
    }

    private void requireItemChangesAllowed() {
        if (!status.allowsItemChanges()) {
            throw new InvalidTransitionException(
                    "Serviços e peças só podem ser alterados com a OS em RECEIVED ou IN_DIAGNOSIS (status atual: "
                            + status + ").");
        }
    }

    private Instant transitionTo(ServiceOrderStatus next, String actor, String note) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("Transition not allowed by the state machine: " + status + " -> " + next);
        }

        Instant now = Instant.now();
        history.add(StatusHistoryEntry.record(status, next, actor, now, note));
        this.status = next;
        return now;
    }

    public UUID getId() {
        return id;
    }

    public ServiceOrderNumber getNumber() {
        return number;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public ServiceOrderStatus getStatus() {
        return status;
    }

    public List<ServiceItem> getServiceItems() {
        return Collections.unmodifiableList(serviceItems);
    }

    public List<PartItem> getPartItems() {
        return Collections.unmodifiableList(partItems);
    }

    public Quote getQuote() {
        return quote;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public List<StatusHistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public Long getVersion() {
        return version;
    }
}
