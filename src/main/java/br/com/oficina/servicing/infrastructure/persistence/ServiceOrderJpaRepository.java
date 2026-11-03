package br.com.oficina.servicing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderJpaEntity, UUID>,
        JpaSpecificationExecutor<ServiceOrderJpaEntity> {

    Optional<ServiceOrderJpaEntity> findByNumber(String number);

    @Query(value = "select nextval('service_order_number_seq')", nativeQuery = true)
    long nextSequence();
}
