package br.com.oficina.registration.application;

import br.com.oficina.registration.domain.CustomerRepository;
import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.registration.domain.VehicleRepository;
import br.com.oficina.shared.domain.exception.BusinessRuleException;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.LicensePlate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public VehicleService(VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Vehicle createVehicle(UUID customerId, String licensePlate, String brand, String model, Integer year) {
        if (customerId == null || !customerRepository.existsById(customerId)) {
            throw new NotFoundException("Cliente não encontrado.");
        }

        LicensePlate plate = new LicensePlate(licensePlate);

        if (vehicleRepository.existsByLicensePlate(plate)) {
            throw new ConflictException("Já existe um veículo cadastrado com esta placa.");
        }

        return vehicleRepository.save(Vehicle.create(customerId, plate, brand, model, year));
    }

    @Transactional
    public Vehicle updateVehicle(UUID id, String brand, String model, Integer year) {
        Vehicle vehicle = findVehicleById(id);
        vehicle.update(brand, model, year);
        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle findVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listVehicles(UUID customerId) {
        if (customerId == null) {
            return vehicleRepository.findAll();
        }
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Cliente não encontrado.");
        }
        return vehicleRepository.findAllByCustomerId(customerId);
    }

    @Transactional
    public void deleteVehicle(UUID id) {
        findVehicleById(id);
        vehicleRepository.delete(id);
    }

    @Transactional
    public Vehicle findOrCreate(UUID customerId, String licensePlate, String brand, String model, Integer year) {
        LicensePlate plate = new LicensePlate(licensePlate);
        Optional<Vehicle> existing = vehicleRepository.findByLicensePlate(plate);

        if (existing.isPresent()) {
            if (!existing.get().belongsTo(customerId)) {
                throw new BusinessRuleException("O veículo de placa " + plate.value() + " pertence a outro cliente.");
            }
            return existing.get();
        }

        if (brand == null || model == null || year == null) {
            throw new ValidationException(
                    "Veículo de placa " + plate.value() + " não cadastrado: informe marca, modelo e ano.");
        }

        return createVehicle(customerId, licensePlate, brand, model, year);
    }
}
