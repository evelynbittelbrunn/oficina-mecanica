package br.com.oficina.catalog.api;

import br.com.oficina.catalog.api.dto.ServiceOfferingRequest;
import br.com.oficina.catalog.api.dto.ServiceOfferingResponse;
import br.com.oficina.catalog.application.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Shop service catalog (writes restricted to ADMIN)")
public class ServiceOfferingController {

    private final CatalogService catalogService;

    public ServiceOfferingController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create service")
    @ApiResponse(responseCode = "201", description = "Service created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "403", description = "Role not allowed (requires ADMIN)")
    public ServiceOfferingResponse createService(@Valid @RequestBody ServiceOfferingRequest request) {
        return ServiceOfferingResponse.from(catalogService.createOffering(
                request.name(), request.description(), request.basePrice(), request.estimatedMinutes()));
    }

    @GetMapping
    @Operation(summary = "List services")
    public List<ServiceOfferingResponse> listServices(
            @Parameter(description = "Filters by status; omit to list all")
            @RequestParam(required = false) Boolean active
    ) {
        return catalogService.listOfferings(active).stream().map(ServiceOfferingResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find service by ID")
    @ApiResponse(responseCode = "200", description = "Service found")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ServiceOfferingResponse findServiceById(@PathVariable UUID id) {
        return ServiceOfferingResponse.from(catalogService.findOfferingById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update service", description = "The `active` field reactivates a deactivated service.")
    @ApiResponse(responseCode = "200", description = "Service updated")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ServiceOfferingResponse updateService(@PathVariable UUID id, @Valid @RequestBody ServiceOfferingRequest request) {
        return ServiceOfferingResponse.from(catalogService.updateOffering(
                id,
                request.name(),
                request.description(),
                request.basePrice(),
                request.estimatedMinutes(),
                request.active()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate service",
            description = "Soft delete: the service is no longer offered, but service order history is preserved.")
    @ApiResponse(responseCode = "204", description = "Service deactivated")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public void deleteService(@PathVariable UUID id) {
        catalogService.deactivateOffering(id);
    }
}
