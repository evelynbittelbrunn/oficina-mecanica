package br.com.oficina.servicing.api.dto;

import br.com.oficina.servicing.domain.PartItem;
import br.com.oficina.servicing.domain.ServiceItem;
import br.com.oficina.servicing.domain.ServiceOrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemResponse(UUID id, String name, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {

    public static ItemResponse from(ServiceItem item) {
        return from(item.getServiceId(), item);
    }

    public static ItemResponse from(PartItem item) {
        return from(item.getInventoryItemId(), item);
    }

    private static ItemResponse from(UUID id, ServiceOrderItem item) {
        return new ItemResponse(id, item.getName(), item.getUnitPrice().amount(), item.getQuantity(),
                item.subtotal().amount());
    }
}
