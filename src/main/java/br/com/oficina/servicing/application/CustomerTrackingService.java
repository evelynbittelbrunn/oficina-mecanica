package br.com.oficina.servicing.application;

import br.com.oficina.inventory.application.StockService;
import br.com.oficina.inventory.application.dto.StockDeduction;
import br.com.oficina.registration.application.CustomerService;
import br.com.oficina.registration.application.VehicleService;
import br.com.oficina.registration.domain.Customer;
import br.com.oficina.servicing.application.dto.ServiceOrderDetails;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderNumber;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.valueobject.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Customer use cases: track the service order and decide the quote. The customer is authenticated by
 * the order number + document pair; if the pair does not match, the order is reported as not found.
 */
@Service
public class CustomerTrackingService {

    public static final String CUSTOMER_ACTOR = "customer";
    private static final String NOT_FOUND = "Ordem de serviço não encontrada para o número e documento informados.";

    private final ServiceOrderRepository repository;
    private final ServiceOrderWriter writer;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final StockService stockService;

    public CustomerTrackingService(
            ServiceOrderRepository repository,
            ServiceOrderWriter writer,
            CustomerService customerService,
            VehicleService vehicleService,
            StockService stockService
    ) {
        this.repository = repository;
        this.writer = writer;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public ServiceOrderDetails track(String number, String document) {
        return details(authenticate(number, document));
    }

    /**
     * Approves the quote and deducts the parts from stock in the same transaction. If stock is
     * insufficient, the whole operation fails (422) and nothing is changed.
     */
    @Transactional
    public ServiceOrderDetails approveQuote(String number, String document) {
        Authenticated authenticated = authenticate(number, document);
        ServiceOrder order = authenticated.order();

        order.approveQuote(CUSTOMER_ACTOR);
        stockService.deduct(
                order.getPartItems().stream()
                        .map(item -> new StockDeduction(item.getInventoryItemId(), item.getQuantity()))
                        .toList(),
                order.getNumber().value());

        writer.save(order);
        return details(authenticated);
    }

    @Transactional
    public ServiceOrderDetails rejectQuote(String number, String document, String reason) {
        Authenticated authenticated = authenticate(number, document);

        authenticated.order().rejectQuote(reason, CUSTOMER_ACTOR);

        writer.save(authenticated.order());
        return details(authenticated);
    }

    private Authenticated authenticate(String number, String document) {
        ServiceOrderNumber orderNumber = new ServiceOrderNumber(number);
        Document informedDocument = new Document(document);

        ServiceOrder order = repository.findByNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND));
        Customer customer = customerService.findCustomerById(order.getCustomerId());

        if (!customer.getDocument().equals(informedDocument)) {
            throw new NotFoundException(NOT_FOUND);
        }

        return new Authenticated(order, customer);
    }

    private ServiceOrderDetails details(Authenticated authenticated) {
        return new ServiceOrderDetails(
                authenticated.order(),
                authenticated.customer(),
                vehicleService.findVehicleById(authenticated.order().getVehicleId()));
    }

    private record Authenticated(ServiceOrder order, Customer customer) {
    }
}
