package br.com.oficina.servicing.infrastructure.persistence;

import br.com.oficina.servicing.domain.PartItem;
import br.com.oficina.servicing.domain.Quote;
import br.com.oficina.servicing.domain.ServiceItem;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderNumber;
import br.com.oficina.servicing.domain.StatusHistoryEntry;
import br.com.oficina.shared.domain.valueobject.Money;

final class ServiceOrderMapper {

    private ServiceOrderMapper() {
    }

    static ServiceOrderJpaEntity toEntity(ServiceOrder order) {
        ServiceOrderJpaEntity entity = new ServiceOrderJpaEntity(
                order.getId(),
                order.getNumber().value(),
                order.getCustomerId(),
                order.getVehicleId(),
                order.getStatus(),
                order.getOpenedAt(),
                order.getDiagnosisNotes(),
                order.getVersion());

        Quote quote = order.getQuote();
        if (quote != null) {
            entity.setQuote(quote.totalAmount().amount(), quote.status(), quote.sentAt(), quote.decidedAt(),
                    quote.rejectionReason());
        }

        order.getServiceItems().forEach(item -> entity.getServiceItems().add(new ServiceItemJpaEntity(
                item.getId(), item.getServiceId(), item.getName(), item.getUnitPrice().amount(), item.getQuantity())));
        order.getPartItems().forEach(item -> entity.getPartItems().add(new PartItemJpaEntity(
                item.getId(), item.getInventoryItemId(), item.getName(), item.getUnitPrice().amount(), item.getQuantity())));
        order.getHistory().forEach(entry -> entity.getHistory().add(new StatusHistoryJpaEntity(
                entry.id(), entry.previousStatus(), entry.newStatus(), entry.changedAt(), entry.actor(), entry.note())));

        return entity;
    }

    static ServiceOrder toDomain(ServiceOrderJpaEntity entity) {
        Quote quote = entity.getQuoteStatus() == null ? null : new Quote(
                new Money(entity.getQuoteTotalAmount()),
                entity.getQuoteStatus(),
                entity.getQuoteSentAt(),
                entity.getQuoteDecidedAt(),
                entity.getQuoteRejectionReason());

        return ServiceOrder.reconstitute(
                entity.getId(),
                new ServiceOrderNumber(entity.getNumber()),
                entity.getCustomerId(),
                entity.getVehicleId(),
                entity.getOpenedAt(),
                entity.getStatus(),
                entity.getServiceItems().stream().map(item -> new ServiceItem(item.getId(), item.getServiceId(),
                        item.getName(), new Money(item.getUnitPrice()), item.getQuantity())).toList(),
                entity.getPartItems().stream().map(item -> new PartItem(item.getId(), item.getInventoryItemId(),
                        item.getName(), new Money(item.getUnitPrice()), item.getQuantity())).toList(),
                quote,
                entity.getDiagnosisNotes(),
                entity.getHistory().stream().map(entry -> new StatusHistoryEntry(entry.getId(),
                        entry.getPreviousStatus(), entry.getNewStatus(), entry.getChangedAt(),
                        entry.getActor(), entry.getNote())).toList(),
                entity.getVersion());
    }
}
