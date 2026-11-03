package br.com.oficina.registration.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.LicensePlate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class VehicleTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final LicensePlate PLATE = new LicensePlate("ABC1D23");

    @Test
    void shouldCreateVehicle() {
        Vehicle vehicle = Vehicle.create(CUSTOMER_ID, PLATE, "  Volkswagen ", " Gol ", 2020);

        assertThat(vehicle.getId()).isNotNull();
        assertThat(vehicle.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(vehicle.getLicensePlate()).isEqualTo(PLATE);
        assertThat(vehicle.getBrand()).isEqualTo("Volkswagen");
        assertThat(vehicle.getModel()).isEqualTo("Gol");
        assertThat(vehicle.getYear()).isEqualTo(2020);
    }

    @Test
    void shouldAcceptYearBoundaries() {
        int nextYear = Year.now().getValue() + 1;

        assertThat(vehicle(1900).getYear()).isEqualTo(1900);
        assertThat(vehicle(nextYear).getYear()).isEqualTo(nextYear);
        assertThat(Vehicle.maxYear()).isEqualTo(nextYear);
    }

    @Test
    void shouldRejectYearOutOfBounds() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> vehicle(1899));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> vehicle(Year.now().getValue() + 2));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> vehicle(null));
    }

    @Test
    void shouldIdentifyOwner() {
        Vehicle vehicle = vehicle(2020);

        assertThat(vehicle.belongsTo(CUSTOMER_ID)).isTrue();
        assertThat(vehicle.belongsTo(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldUpdateBrandModelAndYear() {
        Vehicle vehicle = vehicle(2020);

        vehicle.update("Fiat", "Argo", 2022);

        assertThat(vehicle.getBrand()).isEqualTo("Fiat");
        assertThat(vehicle.getModel()).isEqualTo("Argo");
        assertThat(vehicle.getYear()).isEqualTo(2022);
        assertThat(vehicle.getLicensePlate()).isEqualTo(PLATE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldRejectBlankBrandOrModel(String value) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Vehicle.create(CUSTOMER_ID, PLATE, value, "Gol", 2020));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Vehicle.create(CUSTOMER_ID, PLATE, "VW", value, 2020));
    }

    @Test
    void shouldRejectTooLongText() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Vehicle.create(CUSTOMER_ID, PLATE, "a".repeat(101), "Gol", 2020));
    }

    @Test
    void shouldRejectNullIdentifiers() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Vehicle(null, CUSTOMER_ID, PLATE, "VW", "Gol", 2020));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Vehicle.create(null, PLATE, "VW", "Gol", 2020));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Vehicle.create(CUSTOMER_ID, null, "VW", "Gol", 2020));
    }

    private Vehicle vehicle(Integer year) {
        return Vehicle.create(CUSTOMER_ID, PLATE, "Volkswagen", "Gol", year);
    }
}
