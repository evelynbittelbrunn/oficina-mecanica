package br.com.oficina.registration.domain;

import br.com.oficina.shared.domain.valueobject.LicensePlate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(UUID id);

    Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate);

    boolean existsByLicensePlate(LicensePlate licensePlate);

    boolean existsByCustomerId(UUID customerId);

    List<Vehicle> findAll();

    List<Vehicle> findAllByCustomerId(UUID customerId);

    void delete(UUID id);
}
