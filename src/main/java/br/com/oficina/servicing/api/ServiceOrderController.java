package br.com.oficina.servicing.api;

import br.com.oficina.servicing.api.dto.CompleteDiagnosisRequest;
import br.com.oficina.servicing.api.dto.ItemRequest;
import br.com.oficina.servicing.api.dto.OpenServiceOrderRequest;
import br.com.oficina.servicing.api.dto.PageResponse;
import br.com.oficina.servicing.api.dto.ServiceOrderResponse;
import br.com.oficina.servicing.api.dto.ServiceOrderSummaryResponse;
import br.com.oficina.servicing.application.ServiceOrderService;
import br.com.oficina.servicing.application.dto.OpenServiceOrderCommand;
import br.com.oficina.servicing.application.dto.RequestedItem;
import br.com.oficina.servicing.application.dto.VehicleInput;
import br.com.oficina.servicing.domain.ServiceOrderFilter;
import br.com.oficina.servicing.domain.ServiceOrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-orders")
@Tag(name = "Service orders", description = "Opening, diagnosis, execution and delivery (internal use)")
public class ServiceOrderController {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final ServiceOrderService service;

    public ServiceOrderController(ServiceOrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open service order",
            description = "Identifies the customer by CPF/CNPJ, registers the vehicle if the plate is new and adds "
                    + "the initial services and parts with their current prices. The order starts as RECEIVED.")
    @ApiResponse(responseCode = "201", description = "Service order opened")
    @ApiResponse(responseCode = "400", description = "Invalid data (or new vehicle without brand/model/year)")
    @ApiResponse(responseCode = "404", description = "Customer, service or inventory item not found")
    @ApiResponse(responseCode = "422", description = "Vehicle belongs to another customer or service is inactive")
    public ServiceOrderResponse openServiceOrder(@Valid @RequestBody OpenServiceOrderRequest request,
                                                 @AuthenticationPrincipal Jwt user) {
        OpenServiceOrderRequest.VehicleRequest vehicle = request.vehicle();
        OpenServiceOrderCommand command = new OpenServiceOrderCommand(
                request.customerDocument(),
                new VehicleInput(vehicle.licensePlate(), vehicle.brand(), vehicle.model(), vehicle.year()),
                items(request.services()),
                items(request.parts()));
        return ServiceOrderResponse.from(service.open(command, actor(user)));
    }

    @GetMapping
    @Operation(summary = "List service orders", description = "Paginated, newest first. Period filters by opening date.")
    public PageResponse<ServiceOrderSummaryResponse> listServiceOrders(
            @Parameter(description = "Filters by status") @RequestParam(required = false) ServiceOrderStatus status,
            @Parameter(description = "Filters by customer") @RequestParam(required = false) UUID customerId,
            @Parameter(description = "Opened from (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Opened until (inclusive, yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Page (starts at 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size
    ) {
        ServiceOrderFilter filter = new ServiceOrderFilter(
                status,
                customerId,
                from == null ? null : from.atStartOfDay(ZONE).toInstant(),
                to == null ? null : to.plusDays(1).atStartOfDay(ZONE).toInstant());
        return PageResponse.from(service.list(filter, page, size), ServiceOrderSummaryResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service order details", description = "Items, quote and status history.")
    @ApiResponse(responseCode = "200", description = "Service order found")
    @ApiResponse(responseCode = "404", description = "Service order not found")
    public ServiceOrderResponse getServiceOrder(@PathVariable UUID id) {
        return ServiceOrderResponse.from(service.getDetails(id));
    }

    @PostMapping("/{id}/services")
    @Operation(summary = "Add service", description = "Only in RECEIVED or IN_DIAGNOSIS. A repeated service adds to the quantity.")
    @ApiResponse(responseCode = "200", description = "Service added")
    @ApiResponse(responseCode = "409", description = "Order status does not allow item changes")
    @ApiResponse(responseCode = "422", description = "Inactive service")
    public ServiceOrderResponse addService(@PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
        return ServiceOrderResponse.from(service.addService(id, request.id(), quantity(request)));
    }

    @DeleteMapping("/{id}/services/{serviceId}")
    @Operation(summary = "Remove service", description = "Only in RECEIVED or IN_DIAGNOSIS.")
    @ApiResponse(responseCode = "200", description = "Service removed")
    @ApiResponse(responseCode = "404", description = "Service is not in the order")
    @ApiResponse(responseCode = "409", description = "Order status does not allow item changes")
    public ServiceOrderResponse removeService(@PathVariable UUID id, @PathVariable UUID serviceId) {
        return ServiceOrderResponse.from(service.removeService(id, serviceId));
    }

    @PostMapping("/{id}/parts")
    @Operation(summary = "Add part/supply", description = "Only in RECEIVED or IN_DIAGNOSIS. Stock is deducted on approval.")
    @ApiResponse(responseCode = "200", description = "Part added")
    @ApiResponse(responseCode = "409", description = "Order status does not allow item changes")
    public ServiceOrderResponse addPart(@PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
        return ServiceOrderResponse.from(service.addPart(id, request.id(), quantity(request)));
    }

    @DeleteMapping("/{id}/parts/{inventoryItemId}")
    @Operation(summary = "Remove part/supply", description = "Only in RECEIVED or IN_DIAGNOSIS.")
    @ApiResponse(responseCode = "200", description = "Part removed")
    @ApiResponse(responseCode = "404", description = "Part is not in the order")
    @ApiResponse(responseCode = "409", description = "Order status does not allow item changes")
    public ServiceOrderResponse removePart(@PathVariable UUID id, @PathVariable UUID inventoryItemId) {
        return ServiceOrderResponse.from(service.removePart(id, inventoryItemId));
    }

    @PostMapping("/{id}/start-diagnosis")
    @Operation(summary = "Start diagnosis", description = "RECEIVED → IN_DIAGNOSIS.")
    @ApiResponse(responseCode = "200", description = "Diagnosis started")
    @ApiResponse(responseCode = "409", description = "Invalid transition")
    public ServiceOrderResponse startDiagnosis(@PathVariable UUID id, @AuthenticationPrincipal Jwt user) {
        return ServiceOrderResponse.from(service.startDiagnosis(id, actor(user)));
    }

    @PostMapping("/{id}/complete-diagnosis")
    @Operation(summary = "Complete diagnosis",
            description = "IN_DIAGNOSIS → AWAITING_APPROVAL. Generates the quote (services + parts) and sends it to the customer.")
    @ApiResponse(responseCode = "200", description = "Quote generated and sent")
    @ApiResponse(responseCode = "409", description = "Invalid transition")
    @ApiResponse(responseCode = "422", description = "Order without any service")
    public ServiceOrderResponse completeDiagnosis(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CompleteDiagnosisRequest request,
            @AuthenticationPrincipal Jwt user
    ) {
        String notes = request == null ? null : request.notes();
        return ServiceOrderResponse.from(service.completeDiagnosis(id, notes, actor(user)));
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finish execution", description = "IN_PROGRESS → FINISHED.")
    @ApiResponse(responseCode = "200", description = "Execution finished")
    @ApiResponse(responseCode = "409", description = "Invalid transition")
    public ServiceOrderResponse finish(@PathVariable UUID id, @AuthenticationPrincipal Jwt user) {
        return ServiceOrderResponse.from(service.finish(id, actor(user)));
    }

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Deliver vehicle", description = "FINISHED → DELIVERED.")
    @ApiResponse(responseCode = "200", description = "Vehicle delivered")
    @ApiResponse(responseCode = "409", description = "Invalid transition")
    public ServiceOrderResponse deliver(@PathVariable UUID id, @AuthenticationPrincipal Jwt user) {
        return ServiceOrderResponse.from(service.deliver(id, actor(user)));
    }

    private static List<RequestedItem> items(List<ItemRequest> items) {
        return items == null ? List.of() : items.stream()
                .map(item -> new RequestedItem(item.id(), item.quantity()))
                .toList();
    }

    private static int quantity(ItemRequest request) {
        return request.quantity() == null ? 1 : request.quantity();
    }

    private static String actor(Jwt user) {
        return user == null ? null : user.getSubject();
    }
}
