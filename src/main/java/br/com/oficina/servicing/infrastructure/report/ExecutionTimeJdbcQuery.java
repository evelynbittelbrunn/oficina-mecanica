package br.com.oficina.servicing.infrastructure.report;

import br.com.oficina.servicing.application.ExecutionTimeQuery;
import br.com.oficina.servicing.application.dto.AverageExecutionTime;
import br.com.oficina.servicing.application.dto.ServiceAverageExecutionTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
class ExecutionTimeJdbcQuery implements ExecutionTimeQuery {

    private static final String EXECUTIONS = """
            WITH executions AS (
                SELECT started.service_order_id AS order_id,
                       MIN(started.changed_at)  AS started_at,
                       MIN(finished.changed_at) AS finished_at
                  FROM service_order_status_history started
                  JOIN service_order_status_history finished
                    ON finished.service_order_id = started.service_order_id
                   AND finished.previous_status = 'IN_PROGRESS'
                   AND finished.new_status = 'FINISHED'
                 WHERE started.new_status = 'IN_PROGRESS'
                 GROUP BY started.service_order_id
            ),
            period AS (
                SELECT order_id, EXTRACT(EPOCH FROM (finished_at - started_at)) / 60.0 AS minutes
                  FROM executions
                 WHERE finished_at >= :from AND finished_at < :to
            )
            """;

    private static final String OVERALL = EXECUTIONS + """
            SELECT COUNT(*) AS order_count, AVG(minutes) AS average FROM period
            """;

    private static final String BY_SERVICE = EXECUTIONS + """
            SELECT item.service_id                 AS service_id,
                   MIN(item.name)                  AS name,
                   COUNT(DISTINCT period.order_id) AS order_count,
                   AVG(period.minutes)             AS average
              FROM period
              JOIN service_order_services item ON item.service_order_id = period.order_id
             GROUP BY item.service_id
             ORDER BY name
            """;

    private final NamedParameterJdbcTemplate jdbc;

    ExecutionTimeJdbcQuery(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AverageExecutionTime overall(Instant from, Instant to) {
        return jdbc.queryForObject(OVERALL, parameters(from, to), (row, index) -> new AverageExecutionTime(
                row.getLong("order_count"),
                round((Number) row.getObject("average"))));
    }

    @Override
    public List<ServiceAverageExecutionTime> byService(Instant from, Instant to) {
        return jdbc.query(BY_SERVICE, parameters(from, to), (row, index) -> new ServiceAverageExecutionTime(
                row.getObject("service_id", UUID.class),
                row.getString("name"),
                row.getLong("order_count"),
                round((Number) row.getObject("average"))));
    }

    private static MapSqlParameterSource parameters(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
    }

    private static Double round(Number minutes) {
        return minutes == null ? null : Math.round(minutes.doubleValue() * 100.0) / 100.0;
    }
}
