package br.com.oficina.registration.infrastructure.persistence;

import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.registration.domain.VehicleRepository;
import br.com.oficina.shared.domain.valueobject.LicensePlate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class VehicleRepositoryAdapter implements VehicleRepository {

    private final VehicleJpaRepository jpa;

    VehicleRepositoryAdapter(VehicleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        jpa.save(new VehicleJpaEntity(
                vehicle.getId(),
                vehicle.getCustomerId(),
                vehicle.getLicensePlate().value(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear()
        ));
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpa.findById(id).map(VehicleRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate) {
        return jpa.findByLicensePlate(licensePlate.value()).map(VehicleRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByLicensePlate(LicensePlate licensePlate) {
        return jpa.existsByLicensePlate(licensePlate.value());
    }

    @Override
    public boolean existsByCustomerId(UUID customerId) {
        return jpa.existsByCustomerId(customerId);
    }

    @Override
    public List<Vehicle> findAll() {
        return jpa.findAll(Sort.by("licensePlate")).stream().map(VehicleRepositoryAdapter::toDomain).toList();
    }

    @Override
    public List<Vehicle> findAllByCustomerId(UUID customerId) {
        return jpa.findAllByCustomerIdOrderByLicensePlate(customerId).stream()
                .map(VehicleRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Vehicle toDomain(VehicleJpaEntity entity) {
        return new Vehicle(
                entity.getId(),
                entity.getCustomerId(),
                new LicensePlate(entity.getLicensePlate()),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear()
        );
    }
}
