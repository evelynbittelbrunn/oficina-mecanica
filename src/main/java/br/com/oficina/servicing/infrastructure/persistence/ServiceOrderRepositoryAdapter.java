package br.com.oficina.servicing.infrastructure.persistence;

import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderFilter;
import br.com.oficina.servicing.domain.ServiceOrderNumber;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import br.com.oficina.shared.domain.PageResult;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Transactional so that mapping the lazy collections also works outside an application service. */
@Repository
@Transactional
class ServiceOrderRepositoryAdapter implements ServiceOrderRepository {

    private final ServiceOrderJpaRepository jpa;

    ServiceOrderRepositoryAdapter(ServiceOrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    /** {@code merge} of the whole aggregate; a stale version throws OptimisticLockingFailureException. */
    @Override
    public ServiceOrder save(ServiceOrder order) {
        jpa.save(ServiceOrderMapper.toEntity(order));
        return order;
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return jpa.findById(id).map(ServiceOrderMapper::toDomain);
    }

    @Override
    public Optional<ServiceOrder> findByNumber(ServiceOrderNumber number) {
        return jpa.findByNumber(number.value()).map(ServiceOrderMapper::toDomain);
    }

    @Override
    public PageResult<ServiceOrder> findAll(ServiceOrderFilter filter, int page, int size) {
        Page<ServiceOrderJpaEntity> result = jpa.findAll(
                specification(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "openedAt")));

        return new PageResult<>(
                result.getContent().stream().map(ServiceOrderMapper::toDomain).toList(),
                page,
                size,
                result.getTotalElements());
    }

    @Override
    public long nextSequence() {
        return jpa.nextSequence();
    }

    private static Specification<ServiceOrderJpaEntity> specification(ServiceOrderFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.customerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), filter.customerId()));
            }
            if (filter.openedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("openedAt"), filter.openedFrom()));
            }
            if (filter.openedTo() != null) {
                predicates.add(cb.lessThan(root.get("openedAt"), filter.openedTo()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
