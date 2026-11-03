package br.com.oficina.servicing.api;

import br.com.oficina.servicing.api.dto.QuoteDecisionRequest;
import br.com.oficina.servicing.api.dto.QuoteRejectionRequest;
import br.com.oficina.servicing.api.dto.TrackingResponse;
import br.com.oficina.servicing.application.CustomerTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/public/service-orders")
@SecurityRequirements
@Tag(name = "Tracking (customer)",
        description = "Used by the customer, no JWT: authenticated by service order number + customer CPF/CNPJ")
public class PublicTrackingController {

    private final CustomerTrackingService service;

    public PublicTrackingController(CustomerTrackingService service) {
        this.service = service;
    }

    @GetMapping("/{number}")
    @Operation(summary = "Track service order", description = "Status, items, quote and history.")
    @ApiResponse(responseCode = "200", description = "Service order found")
    @ApiResponse(responseCode = "400", description = "Invalid number or document format")
    @ApiResponse(responseCode = "404", description = "Number and document do not match")
    public TrackingResponse track(
            @Parameter(example = "OS-2026-000001") @PathVariable String number,
            @Parameter(description = "Customer CPF/CNPJ", example = "529.982.247-25")
            @RequestParam @NotBlank(message = "Documento é obrigatório.") String document
    ) {
        return TrackingResponse.from(service.track(number, document));
    }

    @PostMapping("/{number}/quote/approve")
    @Operation(summary = "Approve quote",
            description = "AWAITING_APPROVAL → IN_PROGRESS. Deducts the parts from stock in the same transaction.")
    @ApiResponse(responseCode = "200", description = "Quote approved; execution started")
    @ApiResponse(responseCode = "404", description = "Number and document do not match")
    @ApiResponse(responseCode = "409", description = "Order is not awaiting approval")
    @ApiResponse(responseCode = "422", description = "Insufficient stock; nothing was changed")
    public TrackingResponse approveQuote(
            @Parameter(example = "OS-2026-000001") @PathVariable String number,
            @Valid @RequestBody QuoteDecisionRequest request
    ) {
        return TrackingResponse.from(service.approveQuote(number, request.document()));
    }

    @PostMapping("/{number}/quote/reject")
    @Operation(summary = "Reject quote",
            description = "AWAITING_APPROVAL → FINISHED without execution; records the reason and does not move stock.")
    @ApiResponse(responseCode = "200", description = "Quote rejected")
    @ApiResponse(responseCode = "404", description = "Number and document do not match")
    @ApiResponse(responseCode = "409", description = "Order is not awaiting approval")
    public TrackingResponse rejectQuote(
            @Parameter(example = "OS-2026-000001") @PathVariable String number,
            @Valid @RequestBody QuoteRejectionRequest request
    ) {
        return TrackingResponse.from(service.rejectQuote(number, request.document(), request.reason()));
    }
}
