package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {

    ServiceOrder save(ServiceOrder serviceOrder);

    Optional<ServiceOrder> findById(UUID id);

    Optional<ServiceOrder> findByNumber(ServiceOrderNumber number);

    PageResult<ServiceOrder> findAll(ServiceOrderFilter filter, int page, int size);

    long nextSequence();
}
