package br.com.oficina.registration.api.dto;

import br.com.oficina.registration.domain.Vehicle;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID customerId,
        String licensePlate,
        String brand,
        String model,
        int year
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getCustomerId(),
                vehicle.getLicensePlate().value(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear()
        );
    }
}
