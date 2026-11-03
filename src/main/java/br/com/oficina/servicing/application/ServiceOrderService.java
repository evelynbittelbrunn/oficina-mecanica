package br.com.oficina.servicing.application;

import br.com.oficina.catalog.application.CatalogService;
import br.com.oficina.catalog.domain.ServiceOffering;
import br.com.oficina.inventory.application.InventoryItemService;
import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.registration.application.CustomerService;
import br.com.oficina.registration.application.VehicleService;
import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.servicing.application.dto.OpenServiceOrderCommand;
import br.com.oficina.servicing.application.dto.RequestedItem;
import br.com.oficina.servicing.application.dto.ServiceOrderDetails;
import br.com.oficina.servicing.application.dto.VehicleInput;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderFilter;
import br.com.oficina.servicing.domain.ServiceOrderNumber;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import br.com.oficina.shared.domain.PageResult;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Year;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class ServiceOrderService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceOrderRepository repository;
    private final ServiceOrderWriter writer;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final CatalogService catalogService;
    private final InventoryItemService inventoryItemService;
    private final Clock clock;

    public ServiceOrderService(
            ServiceOrderRepository repository,
            ServiceOrderWriter writer,
            CustomerService customerService,
            VehicleService vehicleService,
            CatalogService catalogService,
            InventoryItemService inventoryItemService,
            Clock clock
    ) {
        this.repository = repository;
        this.writer = writer;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.catalogService = catalogService;
        this.inventoryItemService = inventoryItemService;
        this.clock = clock;
    }

    @Transactional
    public ServiceOrderDetails open(OpenServiceOrderCommand command, String actor) {
        VehicleInput vehicleInput = command.vehicle();
        if (vehicleInput == null) {
            throw new ValidationException("Informe o veículo da ordem de serviço.");
        }

        Customer customer = customerService.findCustomerByDocument(command.customerDocument());
        Vehicle vehicle = vehicleService.findOrCreate(customer.getId(), vehicleInput.licensePlate(),
                vehicleInput.brand(), vehicleInput.model(), vehicleInput.year());

        ServiceOrderNumber number = ServiceOrderNumber.generate(Year.now(clock).getValue(), repository.nextSequence());
        ServiceOrder order = ServiceOrder.open(number, customer.getId(), vehicle.getId(), actor);

        command.services().forEach(item -> addService(order, item.id(), quantity(item)));
        command.parts().forEach(item -> addPart(order, item.id(), quantity(item)));

        return new ServiceOrderDetails(writer.save(order), customer, vehicle);
    }

    @Transactional
    public ServiceOrderDetails addService(UUID orderId, UUID serviceId, int quantity) {
        return change(orderId, order -> addService(order, serviceId, quantity));
    }

    @Transactional
    public ServiceOrderDetails removeService(UUID orderId, UUID serviceId) {
        return change(orderId, order -> order.removeService(serviceId));
    }

    @Transactional
    public ServiceOrderDetails addPart(UUID orderId, UUID inventoryItemId, int quantity) {
        return change(orderId, order -> addPart(order, inventoryItemId, quantity));
    }

    @Transactional
    public ServiceOrderDetails removePart(UUID orderId, UUID inventoryItemId) {
        return change(orderId, order -> order.removePart(inventoryItemId));
    }

    @Transactional
    public ServiceOrderDetails startDiagnosis(UUID orderId, String actor) {
        return change(orderId, order -> order.startDiagnosis(actor));
    }

    @Transactional
    public ServiceOrderDetails completeDiagnosis(UUID orderId, String notes, String actor) {
        return change(orderId, order -> order.completeDiagnosis(notes, actor));
    }

    @Transactional
    public ServiceOrderDetails finish(UUID orderId, String actor) {
        return change(orderId, order -> order.finishExecution(actor));
    }

    @Transactional
    public ServiceOrderDetails deliver(UUID orderId, String actor) {
        return change(orderId, order -> order.deliver(actor));
    }

    @Transactional(readOnly = true)
    public ServiceOrderDetails getDetails(UUID orderId) {
        return details(find(orderId));
    }

    @Transactional(readOnly = true)
    public PageResult<ServiceOrder> list(ServiceOrderFilter filter, int page, int size) {
        if (page < 0) {
            throw new ValidationException("Página deve ser maior ou igual a zero.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException("Tamanho da página deve estar entre 1 e " + MAX_PAGE_SIZE + ".");
        }
        if (filter.openedFrom() != null && filter.openedTo() != null && filter.openedFrom().isAfter(filter.openedTo())) {
            throw new ValidationException("Data inicial do período deve ser anterior à data final.");
        }
        return repository.findAll(filter, page, size);
    }

    private ServiceOrderDetails change(UUID orderId, Consumer<ServiceOrder> operation) {
        ServiceOrder order = find(orderId);
        operation.accept(order);
        return details(writer.save(order));
    }

    private ServiceOrder find(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada."));
    }

    private ServiceOrderDetails details(ServiceOrder order) {
        return new ServiceOrderDetails(
                order,
                customerService.findCustomerById(order.getCustomerId()),
                vehicleService.findVehicleById(order.getVehicleId()));
    }

    private void addService(ServiceOrder order, UUID serviceId, int quantity) {
        ServiceOffering offering = catalogService.findActiveOffering(serviceId);
        order.addService(offering.getId(), offering.getName(), offering.getBasePrice(), quantity);
    }

    private void addPart(ServiceOrder order, UUID inventoryItemId, int quantity) {
        InventoryItem item = inventoryItemService.findItemById(inventoryItemId);
        order.addPart(item.getId(), item.getName(), item.getUnitPrice(), quantity);
    }

    private static int quantity(RequestedItem item) {
        if (item.id() == null) {
            throw new ValidationException("Informe o id de cada serviço/peça.");
        }
        return item.quantity() == null ? 1 : item.quantity();
    }
}
