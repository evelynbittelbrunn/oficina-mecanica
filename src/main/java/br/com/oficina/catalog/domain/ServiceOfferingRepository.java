package br.com.oficina.catalog.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOfferingRepository {

    ServiceOffering save(ServiceOffering offering);

    Optional<ServiceOffering> findById(UUID id);

    List<ServiceOffering> findAll(Boolean active);
}
