package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.valueobject.Money;

import java.util.UUID;

public final class ServiceItem extends ServiceOrderItem {

    public ServiceItem(UUID id, UUID serviceId, String name, Money unitPrice, int quantity) {
        super(id, serviceId, name, unitPrice, quantity);
    }

    static ServiceItem create(UUID serviceId, String name, Money unitPrice, int quantity) {
        return new ServiceItem(UUID.randomUUID(), serviceId, name, unitPrice, quantity);
    }

    public UUID getServiceId() {
        return getReferenceId();
    }
}
