package br.com.oficina.servicing.application;

import br.com.oficina.catalog.application.CatalogService;
import br.com.oficina.catalog.domain.ServiceOffering;
import br.com.oficina.inventory.application.InventoryItemService;
import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemType;
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
import br.com.oficina.servicing.domain.ServiceOrderStatus;
import br.com.oficina.shared.domain.PageResult;
import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Document;
import br.com.oficina.shared.domain.valueobject.Email;
import br.com.oficina.shared.domain.valueobject.LicensePlate;
import br.com.oficina.shared.domain.valueobject.Money;
import br.com.oficina.shared.domain.valueobject.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    private static final String ACTOR = "atendente@oficina.com";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ServiceOrderRepository repository;
    @Mock
    private ServiceOrderWriter writer;
    @Mock
    private CustomerService customerService;
    @Mock
    private VehicleService vehicleService;
    @Mock
    private CatalogService catalogService;
    @Mock
    private InventoryItemService inventoryItemService;

    private ServiceOrderService service;

    private final Customer customer = Customer.create("Maria", new Document("52998224725"),
            new Email("maria@email.com"), new Phone("11988887777"));
    private final Vehicle vehicle = Vehicle.create(customer.getId(), new LicensePlate("ABC1D23"), "VW", "Gol", 2020);
    private final ServiceOffering oilChange = ServiceOffering.create("Troca de óleo", null, Money.of("120.00"), 45);
    private final InventoryItem filter = InventoryItem.create("FLT-001", "Filtro", InventoryItemType.PART, Money.of("35.90"), 1);

    @BeforeEach
    void setUp() {
        service = new ServiceOrderService(repository, writer, customerService, vehicleService, catalogService,
                inventoryItemService, CLOCK);
        lenient().when(writer.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(customerService.findCustomerById(customer.getId())).thenReturn(customer);
        lenient().when(vehicleService.findVehicleById(vehicle.getId())).thenReturn(vehicle);
    }

    @Test
    void shouldOpenOrderWithYearNumberInitialItemsAndVehicle() {
        when(customerService.findCustomerByDocument("529.982.247-25")).thenReturn(customer);
        when(vehicleService.findOrCreate(customer.getId(), "ABC1D23", "VW", "Gol", 2020)).thenReturn(vehicle);
        when(repository.nextSequence()).thenReturn(42L);
        when(catalogService.findActiveOffering(oilChange.getId())).thenReturn(oilChange);
        when(inventoryItemService.findItemById(filter.getId())).thenReturn(filter);

        ServiceOrderDetails details = service.open(new OpenServiceOrderCommand(
                "529.982.247-25",
                new VehicleInput("ABC1D23", "VW", "Gol", 2020),
                List.of(new RequestedItem(oilChange.getId(), null)),
                List.of(new RequestedItem(filter.getId(), 2))), ACTOR);

        ServiceOrder order = details.order();
        assertThat(order.getNumber()).isEqualTo(new ServiceOrderNumber("OS-2026-000042"));
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getServiceItems()).singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(1));
        assertThat(order.getPartItems()).singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2));
        assertThat(order.calculateTotal()).isEqualTo(Money.of("191.80"));
        assertThat(order.getHistory().getFirst().actor()).isEqualTo(ACTOR);
        assertThat(details.customer()).isSameAs(customer);
        assertThat(details.vehicle()).isSameAs(vehicle);
        verify(writer).save(order);
    }

    @Test
    void shouldRequireVehicleAndItemIds() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.open(new OpenServiceOrderCommand("52998224725", null, null, null), ACTOR));

        when(customerService.findCustomerByDocument("52998224725")).thenReturn(customer);
        when(vehicleService.findOrCreate(any(), any(), any(), any(), any())).thenReturn(vehicle);
        when(repository.nextSequence()).thenReturn(1L);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.open(new OpenServiceOrderCommand("52998224725",
                        new VehicleInput("ABC1D23", null, null, null),
                        List.of(new RequestedItem(null, 1)), null), ACTOR));
        verify(writer, never()).save(any());
    }

    @Test
    void shouldAddAndRemoveItems() {
        ServiceOrder order = order();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        when(catalogService.findActiveOffering(oilChange.getId())).thenReturn(oilChange);
        when(inventoryItemService.findItemById(filter.getId())).thenReturn(filter);

        service.addService(order.getId(), oilChange.getId(), 2);
        service.addPart(order.getId(), filter.getId(), 1);
        assertThat(order.calculateTotal()).isEqualTo(Money.of("275.90"));

        service.removePart(order.getId(), filter.getId());
        ServiceOrderDetails details = service.removeService(order.getId(), oilChange.getId());

        assertThat(details.order().getServiceItems()).isEmpty();
        assertThat(details.order().getPartItems()).isEmpty();
    }

    @Test
    void shouldRunTransitionsWithGivenActor() {
        ServiceOrder order = order();
        order.addService(oilChange.getId(), "Troca", Money.of("120"), 1);
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));

        service.startDiagnosis(order.getId(), "mecanico@oficina.com");
        service.completeDiagnosis(order.getId(), "No remarks", "mecanico@oficina.com");
        order.approveQuote("customer");
        service.finish(order.getId(), "mecanico@oficina.com");
        ServiceOrderDetails details = service.deliver(order.getId(), ACTOR);

        assertThat(details.order().getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
        assertThat(order.getHistory().getLast().actor()).isEqualTo(ACTOR);
    }

    @Test
    void invalidTransitionShouldNotSave() {
        ServiceOrder order = order();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> service.deliver(order.getId(), ACTOR));

        verify(writer, never()).save(any());
    }

    @Test
    void shouldReturnDetailsOrFailIfMissing() {
        ServiceOrder order = order();
        UUID missing = UUID.randomUUID();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        when(repository.findById(missing)).thenReturn(Optional.empty());

        assertThat(service.getDetails(order.getId()).order()).isSameAs(order);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.getDetails(missing));
    }

    @Test
    void shouldListValidatingPaginationAndPeriod() {
        PageResult<ServiceOrder> page = new PageResult<>(List.of(order()), 0, 10, 1);
        ServiceOrderFilter filter = ServiceOrderFilter.empty();
        when(repository.findAll(filter, 0, 10)).thenReturn(page);

        assertThat(service.list(filter, 0, 10)).isSameAs(page);

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> service.list(filter, -1, 10));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> service.list(filter, 0, 0));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> service.list(filter, 0, 101));

        Instant now = Instant.now();
        ServiceOrderFilter inverted = new ServiceOrderFilter(null, null, now, now.minusSeconds(1));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> service.list(inverted, 0, 10));

        ServiceOrderFilter period = new ServiceOrderFilter(null, null, now.minusSeconds(1), now);
        when(repository.findAll(period, 0, 10)).thenReturn(page);
        assertThat(service.list(period, 0, 10)).isSameAs(page);
    }

    private ServiceOrder order() {
        return ServiceOrder.open(ServiceOrderNumber.generate(2026, 1), customer.getId(), vehicle.getId(), ACTOR);
    }
}
