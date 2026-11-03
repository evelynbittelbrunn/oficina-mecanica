package br.com.oficina.catalog.application;

import br.com.oficina.catalog.domain.ServiceOffering;
import br.com.oficina.catalog.domain.ServiceOfferingRepository;
import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {

    private final ServiceOfferingRepository repository;

    public CatalogService(ServiceOfferingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServiceOffering createOffering(String name, String description, BigDecimal basePrice, Integer estimatedMinutes) {
        return repository.save(ServiceOffering.create(name, description, money(basePrice), estimatedMinutes));
    }

    @Transactional
    public ServiceOffering updateOffering(
            UUID id,
            String name,
            String description,
            BigDecimal basePrice,
            Integer estimatedMinutes,
            Boolean active
    ) {
        ServiceOffering offering = findOfferingById(id);
        offering.update(name, description, money(basePrice), estimatedMinutes);

        if (Boolean.TRUE.equals(active)) {
            offering.activate();
        } else if (Boolean.FALSE.equals(active)) {
            offering.deactivate();
        }

        return repository.save(offering);
    }

    @Transactional(readOnly = true)
    public ServiceOffering findOfferingById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado."));
    }

    @Transactional(readOnly = true)
    public ServiceOffering findActiveOffering(UUID id) {
        ServiceOffering offering = findOfferingById(id);

        if (!offering.isActive()) {
            throw new BusinessRuleException("Serviço \"" + offering.getName() + "\" está inativo e não pode ser incluído.");
        }

        return offering;
    }

    @Transactional(readOnly = true)
    public List<ServiceOffering> listOfferings(Boolean active) {
        return repository.findAll(active);
    }

    @Transactional
    public void deactivateOffering(UUID id) {
        ServiceOffering offering = findOfferingById(id);
        offering.deactivate();
        repository.save(offering);
    }

    private static Money money(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }
}
