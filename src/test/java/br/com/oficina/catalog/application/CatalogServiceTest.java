package br.com.oficina.catalog.application;

import br.com.oficina.catalog.domain.ServiceOffering;
import br.com.oficina.catalog.domain.ServiceOfferingRepository;
import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ServiceOfferingRepository repository;

    @InjectMocks
    private CatalogService service;

    @Test
    void shouldCreateService() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOffering offering = service.createOffering("Troca de óleo", "Óleo e filtro", new BigDecimal("120"), 45);

        assertThat(offering.getBasePrice()).isEqualTo(Money.of("120.00"));
        assertThat(offering.isActive()).isTrue();
    }

    @Test
    void shouldRejectMissingPrice() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createOffering("Troca de óleo", null, null, 45));
    }

    @Test
    void shouldUpdateAndChangeStatus() {
        ServiceOffering offering = offering();
        when(repository.findById(offering.getId())).thenReturn(Optional.of(offering));
        when(repository.save(offering)).thenReturn(offering);

        service.updateOffering(offering.getId(), "Troca", null, new BigDecimal("130"), 40, false);
        assertThat(offering.isActive()).isFalse();

        service.updateOffering(offering.getId(), "Troca", null, new BigDecimal("130"), 40, true);
        assertThat(offering.isActive()).isTrue();

        service.updateOffering(offering.getId(), "Troca", null, new BigDecimal("130"), 40, null);
        assertThat(offering.isActive()).isTrue();
        assertThat(offering.getBasePrice()).isEqualTo(Money.of("130"));
    }

    @Test
    void shouldFailForMissingService() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.findOfferingById(id));
    }

    @Test
    void findActiveShouldRejectInactiveService() {
        ServiceOffering active = offering();
        ServiceOffering inactive = offering();
        inactive.deactivate();
        when(repository.findById(active.getId())).thenReturn(Optional.of(active));
        when(repository.findById(inactive.getId())).thenReturn(Optional.of(inactive));

        assertThat(service.findActiveOffering(active.getId())).isSameAs(active);
        assertThatExceptionOfType(BusinessRuleException.class).isThrownBy(() -> service.findActiveOffering(inactive.getId()));
    }

    @Test
    void shouldListWithFilter() {
        List<ServiceOffering> offerings = List.of(offering());
        when(repository.findAll(true)).thenReturn(offerings);

        assertThat(service.listOfferings(true)).isEqualTo(offerings);
    }

    @Test
    void shouldDeactivateOnDelete() {
        ServiceOffering offering = offering();
        when(repository.findById(offering.getId())).thenReturn(Optional.of(offering));

        service.deactivateOffering(offering.getId());

        assertThat(offering.isActive()).isFalse();
        verify(repository).save(offering);
    }

    private ServiceOffering offering() {
        return ServiceOffering.create("Troca de óleo", null, Money.of("120.00"), 45);
    }
}
