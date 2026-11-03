package br.com.oficina.registration.api;

import br.com.oficina.registration.api.dto.CreateVehicleRequest;
import br.com.oficina.registration.api.dto.UpdateVehicleRequest;
import br.com.oficina.registration.api.dto.VehicleResponse;
import br.com.oficina.registration.application.VehicleService;
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
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Customer vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create vehicle", description = "The license plate (old or Mercosur format) must be unique.")
    @ApiResponse(responseCode = "201", description = "Vehicle created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "409", description = "License plate already registered")
    public VehicleResponse createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return VehicleResponse.from(vehicleService.createVehicle(
                request.customerId(), request.licensePlate(), request.brand(), request.model(), request.year()));
    }

    @GetMapping
    @Operation(summary = "List vehicles", description = "Lists all vehicles or only a customer's vehicles.")
    public List<VehicleResponse> listVehicles(
            @Parameter(description = "Filters by customer")
            @RequestParam(required = false) UUID customerId
    ) {
        return vehicleService.listVehicles(customerId).stream().map(VehicleResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find vehicle by ID")
    @ApiResponse(responseCode = "200", description = "Vehicle found")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    public VehicleResponse findVehicleById(@PathVariable UUID id) {
        return VehicleResponse.from(vehicleService.findVehicleById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Updates brand, model and year.")
    @ApiResponse(responseCode = "200", description = "Vehicle updated")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    public VehicleResponse updateVehicle(@PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest request) {
        return VehicleResponse.from(vehicleService.updateVehicle(id, request.brand(), request.model(), request.year()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete vehicle", description = "Only allowed for vehicles without service orders.")
    @ApiResponse(responseCode = "204", description = "Vehicle deleted")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @ApiResponse(responseCode = "409", description = "Vehicle has service orders")
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
    }
}
