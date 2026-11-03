package br.com.oficina.catalog.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ServiceOfferingTest {

    private static final Money PRICE = Money.of("120.00");

    @Test
    void shouldCreateActiveService() {
        ServiceOffering offering = ServiceOffering.create(" Troca de óleo ", " Óleo e filtro ", PRICE, 45);

        assertThat(offering.getId()).isNotNull();
        assertThat(offering.getName()).isEqualTo("Troca de óleo");
        assertThat(offering.getDescription()).isEqualTo("Óleo e filtro");
        assertThat(offering.getBasePrice()).isEqualTo(PRICE);
        assertThat(offering.getEstimatedMinutes()).isEqualTo(45);
        assertThat(offering.isActive()).isTrue();
    }

    @Test
    void blankDescriptionShouldBecomeNull() {
        assertThat(ServiceOffering.create("Troca de óleo", "  ", PRICE, 45).getDescription()).isNull();
        assertThat(ServiceOffering.create("Troca de óleo", null, PRICE, 45).getDescription()).isNull();
    }

    @Test
    void shouldUpdateData() {
        ServiceOffering offering = ServiceOffering.create("Troca de óleo", null, PRICE, 45);

        offering.update("Troca de óleo sintético", "Com filtro", Money.of("150.00"), 50);

        assertThat(offering.getName()).isEqualTo("Troca de óleo sintético");
        assertThat(offering.getBasePrice()).isEqualTo(Money.of("150.00"));
        assertThat(offering.getEstimatedMinutes()).isEqualTo(50);
    }

    @Test
    void shouldDeactivateAndActivate() {
        ServiceOffering offering = ServiceOffering.create("Troca de óleo", null, PRICE, 45);

        offering.deactivate();
        assertThat(offering.isActive()).isFalse();

        offering.activate();
        assertThat(offering.isActive()).isTrue();
    }

    @Test
    void shouldRejectInvalidName() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOffering.create(" ", null, PRICE, 45));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOffering.create(null, null, PRICE, 45));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> ServiceOffering.create("a".repeat(121), null, PRICE, 45));
    }

    @Test
    void shouldRejectTooLongDescription() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> ServiceOffering.create("Troca", "a".repeat(501), PRICE, 45));
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> ServiceOffering.create("Troca", null, null, 45));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -10})
    void shouldRejectInvalidEstimatedTime(Integer minutes) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> ServiceOffering.create("Troca", null, PRICE, minutes));
    }

    @Test
    void shouldRejectNullId() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new ServiceOffering(null, "Troca", null, PRICE, 45, true));
    }
}
