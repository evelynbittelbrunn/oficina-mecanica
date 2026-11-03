package br.com.oficina.servicing.application;

import br.com.oficina.servicing.application.dto.AverageExecutionTime;
import br.com.oficina.servicing.application.dto.ExecutionTimeReport;
import br.com.oficina.servicing.application.dto.ServiceAverageExecutionTime;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderNumber;
import br.com.oficina.servicing.domain.ServiceOrderRepository;
import br.com.oficina.servicing.domain.event.ServiceOrderOpened;
import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ExecutionTimeQuery query;

    @InjectMocks
    private ReportService service;

    @Test
    void shouldConvertInclusivePeriodInTheShopTimeZone() {
        Instant start = Instant.parse("2026-01-01T03:00:00Z");
        Instant exclusiveEnd = Instant.parse("2026-02-01T03:00:00Z");
        AverageExecutionTime overall = new AverageExecutionTime(2, 90.5);
        List<ServiceAverageExecutionTime> byService = List.of(new ServiceAverageExecutionTime(UUID.randomUUID(), "Troca", 2, 90.5));
        when(query.overall(start, exclusiveEnd)).thenReturn(overall);
        when(query.byService(start, exclusiveEnd)).thenReturn(byService);

        ExecutionTimeReport report = service.averageExecutionTime(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(report.from()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(report.to()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(report.overall()).isEqualTo(overall);
        assertThat(report.byService()).isEqualTo(byService);
    }

    @Test
    void withoutPeriodShouldConsiderTheWholeHistory() {
        when(query.overall(any(), any())).thenReturn(new AverageExecutionTime(0, null));
        when(query.byService(any(), any())).thenReturn(List.of());

        ExecutionTimeReport report = service.averageExecutionTime(null, null);

        assertThat(report.overall().averageMinutes()).isNull();
        verify(query).overall(Instant.EPOCH, LocalDate.of(9999, 12, 31).atStartOfDay(ReportService.ZONE).toInstant());
    }

    @Test
    void shouldRejectInvertedPeriod() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.averageExecutionTime(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)));
        verifyNoInteractions(query);
    }

    @Test
    void writerShouldSaveAndPublishPendingEvents() {
        ServiceOrderRepository repository = mock(ServiceOrderRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ServiceOrder order = ServiceOrder.open(ServiceOrderNumber.generate(2026, 1), UUID.randomUUID(),
                UUID.randomUUID(), "attendant");
        when(repository.save(order)).thenReturn(order);

        new ServiceOrderWriter(repository, publisher).save(order);

        verify(publisher).publishEvent(any(ServiceOrderOpened.class));
        assertThat(order.pullEvents()).isEmpty();
    }
}
