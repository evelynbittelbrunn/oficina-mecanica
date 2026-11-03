package br.com.oficina.catalog.infrastructure.persistence;

import br.com.oficina.catalog.domain.ServiceOffering;
import br.com.oficina.catalog.domain.ServiceOfferingRepository;
import br.com.oficina.shared.domain.valueobject.Money;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class ServiceOfferingRepositoryAdapter implements ServiceOfferingRepository {

    private final ServiceOfferingJpaRepository jpa;

    ServiceOfferingRepositoryAdapter(ServiceOfferingJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ServiceOffering save(ServiceOffering offering) {
        jpa.save(new ServiceOfferingJpaEntity(
                offering.getId(),
                offering.getName(),
                offering.getDescription(),
                offering.getBasePrice().amount(),
                offering.getEstimatedMinutes(),
                offering.isActive()
        ));
        return offering;
    }

    @Override
    public Optional<ServiceOffering> findById(UUID id) {
        return jpa.findById(id).map(ServiceOfferingRepositoryAdapter::toDomain);
    }

    @Override
    public List<ServiceOffering> findAll(Boolean active) {
        List<ServiceOfferingJpaEntity> entities = active == null
                ? jpa.findAllByOrderByName()
                : jpa.findAllByActiveOrderByName(active);
        return entities.stream().map(ServiceOfferingRepositoryAdapter::toDomain).toList();
    }

    private static ServiceOffering toDomain(ServiceOfferingJpaEntity entity) {
        return new ServiceOffering(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new Money(entity.getBasePrice()),
                entity.getEstimatedMinutes(),
                entity.isActive()
        );
    }
}
