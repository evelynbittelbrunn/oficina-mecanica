package br.com.oficina.registration.application;

import br.com.oficina.registration.domain.CustomerRepository;
import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.registration.domain.VehicleRepository;
import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.LicensePlate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final LicensePlate PLATE = new LicensePlate("ABC1D23");

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VehicleService service;

    @Test
    void shouldCreateVehicleWithNormalizedPlate() {
        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(vehicleRepository.existsByLicensePlate(PLATE)).thenReturn(false);
        when(vehicleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle vehicle = service.createVehicle(CUSTOMER_ID, "abc-1d23", "VW", "Gol", 2020);

        assertThat(vehicle.getLicensePlate()).isEqualTo(PLATE);
        assertThat(vehicle.getCustomerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    void shouldRejectMissingOrNullCustomer() {
        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.createVehicle(CUSTOMER_ID, "ABC1D23", "VW", "Gol", 2020));
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.createVehicle(null, "ABC1D23", "VW", "Gol", 2020));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicatedPlate() {
        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(vehicleRepository.existsByLicensePlate(PLATE)).thenReturn(true);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> service.createVehicle(CUSTOMER_ID, "ABC1D23", "VW", "Gol", 2020));
    }

    @Test
    void shouldUpdateVehicle() {
        Vehicle vehicle = vehicle();
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        assertThat(service.updateVehicle(vehicle.getId(), "Fiat", "Argo", 2022).getModel()).isEqualTo("Argo");
    }

    @Test
    void shouldFailWhenVehicleIsMissing() {
        UUID id = UUID.randomUUID();
        when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.findVehicleById(id));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.deleteVehicle(id));
    }

    @Test
    void shouldListAllOrByCustomer() {
        List<Vehicle> vehicles = List.of(vehicle());
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(vehicleRepository.findAllByCustomerId(CUSTOMER_ID)).thenReturn(vehicles);

        assertThat(service.listVehicles(null)).isEqualTo(vehicles);
        assertThat(service.listVehicles(CUSTOMER_ID)).isEqualTo(vehicles);
    }

    @Test
    void shouldFailListingByMissingCustomer() {
        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.listVehicles(CUSTOMER_ID));
    }

    @Test
    void shouldDeleteVehicle() {
        Vehicle vehicle = vehicle();
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        service.deleteVehicle(vehicle.getId());

        verify(vehicleRepository).delete(vehicle.getId());
    }

    @Test
    void findOrCreateShouldReturnCustomersExistingVehicle() {
        Vehicle existing = vehicle();
        when(vehicleRepository.findByLicensePlate(PLATE)).thenReturn(Optional.of(existing));

        assertThat(service.findOrCreate(CUSTOMER_ID, "ABC-1D23", null, null, null)).isSameAs(existing);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void findOrCreateShouldRejectVehicleOfAnotherCustomer() {
        when(vehicleRepository.findByLicensePlate(PLATE)).thenReturn(Optional.of(vehicle()));

        assertThatExceptionOfType(BusinessRuleException.class)
                .isThrownBy(() -> service.findOrCreate(UUID.randomUUID(), "ABC1D23", null, null, null));
    }

    @Test
    void findOrCreateShouldRegisterNewVehicle() {
        when(vehicleRepository.findByLicensePlate(PLATE)).thenReturn(Optional.empty());
        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(vehicleRepository.existsByLicensePlate(PLATE)).thenReturn(false);
        when(vehicleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle vehicle = service.findOrCreate(CUSTOMER_ID, "ABC1D23", "VW", "Gol", 2020);

        assertThat(vehicle.getLicensePlate()).isEqualTo(PLATE);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void findOrCreateShouldRequireNewVehicleData() {
        when(vehicleRepository.findByLicensePlate(PLATE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.findOrCreate(CUSTOMER_ID, "ABC1D23", null, "Gol", 2020));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.findOrCreate(CUSTOMER_ID, "ABC1D23", "VW", null, 2020));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.findOrCreate(CUSTOMER_ID, "ABC1D23", "VW", "Gol", null));
    }

    private Vehicle vehicle() {
        return Vehicle.create(CUSTOMER_ID, PLATE, "VW", "Gol", 2020);
    }
}
