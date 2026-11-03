package br.com.oficina.servicing.application;

import br.com.oficina.servicing.application.dto.ExecutionTimeReport;
import br.com.oficina.shared.domain.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ReportService {

    /** Time zone used to interpret the filter dates (shop located in Brazil). */
    static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Instant NO_LOWER_BOUND = Instant.EPOCH;
    private static final Instant NO_UPPER_BOUND = LocalDate.of(9999, 12, 31).atStartOfDay(ZONE).toInstant();

    private final ExecutionTimeQuery query;

    public ReportService(ExecutionTimeQuery query) {
        this.query = query;
    }

    @Transactional(readOnly = true)
    public ExecutionTimeReport averageExecutionTime(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("Data inicial do período deve ser anterior ou igual à data final.");
        }

        Instant start = from == null ? NO_LOWER_BOUND : from.atStartOfDay(ZONE).toInstant();
        Instant end = to == null ? NO_UPPER_BOUND : to.plusDays(1).atStartOfDay(ZONE).toInstant();

        return new ExecutionTimeReport(from, to, query.overall(start, end), query.byService(start, end));
    }
}
