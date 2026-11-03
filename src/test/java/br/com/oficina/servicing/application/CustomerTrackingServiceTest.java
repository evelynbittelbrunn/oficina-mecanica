package br.com.oficina.servicing.application;

import br.com.oficina.inventory.application.StockService;
import br.com.oficina.inventory.application.dto.StockDeduction;
import br.com.oficina.inventory.domain.InsufficientStockException;
import br.com.oficina.registration.application.CustomerService;
import br.com.oficina.registration.application.VehicleService;
import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.servicing.domain.QuoteStatus;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderNumber;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import br.com.oficina.servicing.domain.ServiceOrderStatus;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerTrackingServiceTest {

    private static final String CPF = "529.982.247-25";
    private static final UUID FILTER = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository repository;
    @Mock
    private ServiceOrderWriter writer;
    @Mock
    private CustomerService customerService;
    @Mock
    private VehicleService vehicleService;
    @Mock
    private StockService stockService;

    @InjectMocks
    private CustomerTrackingService service;

    private final Customer customer = Customer.create("Maria", new Document(CPF), new Email("maria@email.com"),
            new Phone("11988887777"));
    private final Vehicle vehicle = Vehicle.create(customer.getId(), new LicensePlate("ABC1D23"), "VW", "Gol", 2020);
    private ServiceOrder order;

    @BeforeEach
    void setUp() {
        order = ServiceOrder.open(ServiceOrderNumber.generate(2026, 7), customer.getId(), vehicle.getId(), "attendant");
        order.addService(UUID.randomUUID(), "Troca de óleo", Money.of("120"), 1);
        order.addPart(FILTER, "Filtro", Money.of("35.90"), 2);
        order.startDiagnosis("mechanic");
        order.completeDiagnosis(null, "mechanic");

        lenient().when(repository.findByNumber(new ServiceOrderNumber("OS-2026-000007"))).thenReturn(Optional.of(order));
        lenient().when(customerService.findCustomerById(customer.getId())).thenReturn(customer);
        lenient().when(vehicleService.findVehicleById(vehicle.getId())).thenReturn(vehicle);
    }

    @Test
    void shouldTrackWithMatchingNumberAndDocument() {
        assertThat(service.track("os-2026-000007", "52998224725").order()).isSameAs(order);
    }

    @Test
    void anotherCustomersDocumentShouldBeReportedAsNotFound() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.track("OS-2026-000007", "11.222.333/0001-81"))
                .withMessage("Ordem de serviço não encontrada para o número e documento informados.");
    }

    @Test
    void missingNumberShouldBeReportedAsNotFound() {
        when(repository.findByNumber(new ServiceOrderNumber("OS-2026-000999"))).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.track("OS-2026-000999", CPF));
    }

    @Test
    void invalidFormatShouldBeRejected() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> service.track("123", CPF));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> service.track("OS-2026-000007", "123"));
    }

    @Test
    void approvingShouldStartExecutionAndDeductParts() {
        service.approveQuote("OS-2026-000007", CPF);

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
        assertThat(order.getQuote().status()).isEqualTo(QuoteStatus.APPROVED);
        assertThat(order.getHistory().getLast().actor()).isEqualTo(CustomerTrackingService.CUSTOMER_ACTOR);
        verify(stockService).deduct(List.of(new StockDeduction(FILTER, 2)), "OS-2026-000007");
        verify(writer).save(order);
    }

    @Test
    void approvingWithoutStockShouldNotSave() {
        doThrow(new InsufficientStockException("no stock")).when(stockService).deduct(anyList(), anyString());

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> service.approveQuote("OS-2026-000007", CPF));

        verify(writer, never()).save(any());
    }

    @Test
    void rejectingShouldFinishWithoutMovingStock() {
        service.rejectQuote("OS-2026-000007", CPF, "Too expensive");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        assertThat(order.getQuote().rejectionReason()).isEqualTo("Too expensive");
        verifyNoInteractions(stockService);
        verify(writer).save(order);
    }

    @Test
    void decisionOutsideAwaitingApprovalShouldFail() {
        service.rejectQuote("OS-2026-000007", CPF, "Too expensive");

        assertThatExceptionOfType(InvalidTransitionException.class).isThrownBy(() -> service.approveQuote("OS-2026-000007", CPF));
        assertThatExceptionOfType(InvalidTransitionException.class)
                .isThrownBy(() -> service.rejectQuote("OS-2026-000007", CPF, "Again"));
        verifyNoInteractions(stockService);
    }
}
