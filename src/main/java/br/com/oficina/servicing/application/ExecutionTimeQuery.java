package br.com.oficina.servicing.application;

import br.com.oficina.servicing.application.dto.AverageExecutionTime;
import br.com.oficina.servicing.application.dto.ServiceAverageExecutionTime;

import java.time.Instant;
import java.util.List;

/**
 * Query port (read model) for execution time, computed from the status history:
 * from the IN_PROGRESS entry to the FINISHED entry (coming from IN_PROGRESS).
 * The period filters by the finishing date: {@code [from, to)}.
 */
public interface ExecutionTimeQuery {

    AverageExecutionTime overall(Instant from, Instant to);

    List<ServiceAverageExecutionTime> byService(Instant from, Instant to);
}
