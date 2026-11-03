package br.com.oficina.catalog.api.dto;

import br.com.oficina.catalog.domain.ServiceOffering;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceOfferingResponse(
        UUID id,
        String name,
        String description,
        BigDecimal basePrice,
        int estimatedMinutes,
        boolean active
) {

    public static ServiceOfferingResponse from(ServiceOffering offering) {
        return new ServiceOfferingResponse(
                offering.getId(),
                offering.getName(),
                offering.getDescription(),
                offering.getBasePrice().amount(),
                offering.getEstimatedMinutes(),
                offering.isActive()
        );
    }
}
