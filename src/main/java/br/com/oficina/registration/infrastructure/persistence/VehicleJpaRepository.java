package br.com.oficina.registration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {

    Optional<VehicleJpaEntity> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByCustomerId(UUID customerId);

    List<VehicleJpaEntity> findAllByCustomerIdOrderByLicensePlate(UUID customerId);
}
