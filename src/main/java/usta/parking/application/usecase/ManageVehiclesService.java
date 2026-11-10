package usta.parking.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import usta.parking.application.exception.ResourceNotFoundException;
import usta.parking.application.port.in.ManageVehiclesUseCase;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.application.port.out.VehicleRepository;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.Vehicle;
import usta.parking.domain.service.ParkingRulesService;

import java.util.List;
import java.util.Optional;

// Caso de uso: gestionar los vehículos (RF-11 y RF-14)
@ApplicationScoped
public class ManageVehiclesService implements ManageVehiclesUseCase {

    private final VehicleRepository vehicleRepository;
    private final ParkingRecordRepository recordRepository;
    private final ParkingRulesService rules = new ParkingRulesService();

    public ManageVehiclesService(VehicleRepository vehicleRepository,
                                 ParkingRecordRepository recordRepository) {
        this.vehicleRepository = vehicleRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    @Transactional
    public Vehicle create(Vehicle vehicle) {
        rules.validateVehicle(vehicle); // RN-05: deja la placa normalizada
        if (vehicleRepository.existsByPlate(vehicle.getPlate())) {
            throw new BusinessRuleException("Ya existe un vehículo con la placa " + vehicle.getPlate());
        }
        vehicle.setId(null);
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle update(Long id, Vehicle vehicle) {
        Vehicle current = findById(id);
        rules.validateVehicle(vehicle);

        // La nueva placa no puede pertenecer a otro vehículo
        boolean plateTaken = vehicleRepository.findByPlate(vehicle.getPlate())
                .filter(other -> !other.getId().equals(id))
                .isPresent();
        if (plateTaken) {
            throw new BusinessRuleException("Ya existe un vehículo con la placa " + vehicle.getPlate());
        }
        // No se cambia el tipo mientras el vehículo está dentro (rompería RN-04)
        boolean typeChanged = vehicle.getVehicleType() != current.getVehicleType();
        if (typeChanged && recordRepository.findActiveByVehicleId(id).isPresent()) {
            throw new BusinessRuleException(
                    "No se puede cambiar el tipo de vehículo mientras está dentro del parqueadero");
        }

        current.setPlate(vehicle.getPlate());
        current.setVehicleType(vehicle.getVehicleType());
        current.setOwnerName(vehicle.getOwnerName());
        current.setOwnerType(vehicle.getOwnerType());
        return vehicleRepository.save(current);
    }

    @Override
    public Vehicle findById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("El vehículo no existe");
        }
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El vehículo no existe"));
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        String cleanPlate = PlateUtils.clean(plate);
        if (cleanPlate.isEmpty()) {
            return Optional.empty();
        }
        return vehicleRepository.findByPlate(cleanPlate);
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }
}
