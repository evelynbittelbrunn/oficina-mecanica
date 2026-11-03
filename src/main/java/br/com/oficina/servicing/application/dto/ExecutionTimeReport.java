package br.com.oficina.servicing.application.dto;

import java.time.LocalDate;
import java.util.List;

public record ExecutionTimeReport(
        LocalDate from,
        LocalDate to,
        AverageExecutionTime overall,
        List<ServiceAverageExecutionTime> byService
) {
}
