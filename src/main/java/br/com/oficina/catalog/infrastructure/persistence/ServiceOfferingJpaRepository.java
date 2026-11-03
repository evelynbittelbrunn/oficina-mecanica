package br.com.oficina.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOfferingJpaEntity, UUID> {

    List<ServiceOfferingJpaEntity> findAllByOrderByName();

    List<ServiceOfferingJpaEntity> findAllByActiveOrderByName(boolean active);
}
