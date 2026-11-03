package br.com.oficina.registration.api;

import br.com.oficina.registration.api.dto.CreateCustomerRequest;
import br.com.oficina.registration.api.dto.CustomerResponse;
import br.com.oficina.registration.api.dto.UpdateCustomerRequest;
import br.com.oficina.registration.application.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer registration (individuals or companies)")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create customer", description = "The document (CPF/CNPJ) is validated and must be unique.")
    @ApiResponse(responseCode = "201", description = "Customer created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "409", description = "Document already registered")
    public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return CustomerResponse.from(customerService.createCustomer(
                request.name(), request.document(), request.email(), request.phone()));
    }

    @GetMapping
    @Operation(summary = "List customers")
    public List<CustomerResponse> listCustomers() {
        return customerService.listCustomers().stream().map(CustomerResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public CustomerResponse findCustomerById(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.findCustomerById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates name, e-mail and phone.")
    @ApiResponse(responseCode = "200", description = "Customer updated")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public CustomerResponse updateCustomer(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        return CustomerResponse.from(customerService.updateCustomer(id, request.name(), request.email(), request.phone()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete customer", description = "Only allowed for customers without vehicles or service orders.")
    @ApiResponse(responseCode = "204", description = "Customer deleted")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "409", description = "Customer has vehicles or service orders")
    public void deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
    }
}
