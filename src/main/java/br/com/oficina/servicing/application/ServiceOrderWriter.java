package br.com.oficina.servicing.application;

import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class ServiceOrderWriter {

    private final ServiceOrderRepository repository;
    private final ApplicationEventPublisher publisher;

    ServiceOrderWriter(ServiceOrderRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    ServiceOrder save(ServiceOrder order) {
        ServiceOrder saved = repository.save(order);
        order.pullEvents().forEach(publisher::publishEvent);
        return saved;
    }
}
