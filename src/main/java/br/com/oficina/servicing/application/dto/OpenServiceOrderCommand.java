package br.com.oficina.servicing.application.dto;

import java.util.List;

public record OpenServiceOrderCommand(
        String customerDocument,
        VehicleInput vehicle,
        List<RequestedItem> services,
        List<RequestedItem> parts
) {

    public OpenServiceOrderCommand {
        services = services == null ? List.of() : List.copyOf(services);
        parts = parts == null ? List.of() : List.copyOf(parts);
    }
}
