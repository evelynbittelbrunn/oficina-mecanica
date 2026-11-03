package br.com.oficina.inventory.api;

import br.com.oficina.inventory.api.dto.CreateInventoryItemRequest;
import br.com.oficina.inventory.api.dto.InventoryItemResponse;
import br.com.oficina.inventory.api.dto.StockMovementRequest;
import br.com.oficina.inventory.api.dto.StockMovementResponse;
import br.com.oficina.inventory.api.dto.UpdateInventoryItemRequest;
import br.com.oficina.inventory.application.InventoryItemService;
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
@RequestMapping("/api/v1/inventory-items")
@Tag(name = "Inventory", description = "Parts and supplies registration and stock control (writes restricted to ADMIN)")
public class InventoryItemController {

    private final InventoryItemService itemService;

    public InventoryItemController(InventoryItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create part/supply", description = "The initial quantity creates an INBOUND movement.")
    @ApiResponse(responseCode = "201", description = "Item created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "409", description = "Code already registered")
    public InventoryItemResponse createItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        return InventoryItemResponse.from(itemService.createItem(
                request.code(),
                request.name(),
                request.type(),
                request.unitPrice(),
                request.initialQuantity(),
                request.minimumStock()
        ));
    }

    @GetMapping
    @Operation(summary = "List parts and supplies")
    public List<InventoryItemResponse> listItems() {
        return itemService.listItems().stream().map(InventoryItemResponse::from).toList();
    }

    @GetMapping("/low-stock")
    @Operation(summary = "List low-stock items", description = "Items with balance at or below the minimum stock.")
    public List<InventoryItemResponse> listLowStockItems() {
        return itemService.listLowStockItems().stream().map(InventoryItemResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find part/supply by ID")
    @ApiResponse(responseCode = "200", description = "Item found")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public InventoryItemResponse findItemById(@PathVariable UUID id) {
        return InventoryItemResponse.from(itemService.findItemById(id));
    }

    @GetMapping("/{id}/movements")
    @Operation(summary = "Stock movement history", description = "Newest first.")
    public List<StockMovementResponse> listMovements(@PathVariable UUID id) {
        return itemService.listMovements(id).stream().map(StockMovementResponse::from).toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update part/supply", description = "Does not change code or balance (use stock-in/stock-out).")
    @ApiResponse(responseCode = "200", description = "Item updated")
    @ApiResponse(responseCode = "404", description = "Item not found")
    @ApiResponse(responseCode = "409", description = "Concurrent update")
    public InventoryItemResponse updateItem(@PathVariable UUID id, @Valid @RequestBody UpdateInventoryItemRequest request) {
        return InventoryItemResponse.from(itemService.updateItem(
                id, request.name(), request.type(), request.unitPrice(), request.minimumStock()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete part/supply", description = "Not allowed if the item is in a service order.")
    @ApiResponse(responseCode = "204", description = "Item deleted")
    @ApiResponse(responseCode = "404", description = "Item not found")
    @ApiResponse(responseCode = "409", description = "Item referenced by a service order")
    public void deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
    }

    @PostMapping("/{id}/stock-in")
    @Operation(summary = "Register stock in")
    @ApiResponse(responseCode = "200", description = "Stock-in registered; returns the new balance")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public InventoryItemResponse registerStockIn(@PathVariable UUID id, @Valid @RequestBody StockMovementRequest request) {
        return InventoryItemResponse.from(itemService.registerStockIn(id, request.quantity(), request.note()));
    }

    @PostMapping("/{id}/stock-out")
    @Operation(summary = "Register stock out", description = "Manual withdrawal (loss, internal use etc.).")
    @ApiResponse(responseCode = "200", description = "Stock-out registered; returns the new balance")
    @ApiResponse(responseCode = "404", description = "Item not found")
    @ApiResponse(responseCode = "422", description = "Insufficient stock")
    public InventoryItemResponse registerStockOut(@PathVariable UUID id, @Valid @RequestBody StockMovementRequest request) {
        return InventoryItemResponse.from(itemService.registerStockOut(id, request.quantity(), request.note()));
    }
}
