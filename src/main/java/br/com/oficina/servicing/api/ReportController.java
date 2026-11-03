package br.com.oficina.servicing.api;

import br.com.oficina.servicing.application.ReportService;
import br.com.oficina.servicing.application.dto.ExecutionTimeReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Operational indicators (restricted to ADMIN)")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/average-execution-time")
    @Operation(summary = "Average execution time",
            description = "Average, in minutes, between entering IN_PROGRESS and FINISHED, overall and per service. "
                    + "The period filters by the finishing date (inclusive dates, America/Sao_Paulo time zone). "
                    + "Orders with a rejected quote are not included.")
    @ApiResponse(responseCode = "200", description = "Report computed")
    @ApiResponse(responseCode = "400", description = "Invalid period")
    @ApiResponse(responseCode = "403", description = "Role not allowed (requires ADMIN)")
    public ExecutionTimeReport averageExecutionTime(
            @Parameter(description = "Finished from (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Finished until (inclusive, yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.averageExecutionTime(from, to);
    }
}
