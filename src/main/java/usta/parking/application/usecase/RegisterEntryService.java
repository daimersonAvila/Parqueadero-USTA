package usta.parking.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import usta.parking.application.exception.ResourceNotFoundException;
import usta.parking.application.port.in.RegisterEntryUseCase;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.application.port.out.ParkingZoneRepository;
import usta.parking.application.port.out.VehicleRepository;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.model.Vehicle;
import usta.parking.domain.service.ParkingRulesService;

// Caso de uso: registrar la entrada de un vehículo (RF-04 a RF-08).
// Si la placa no existe, el vehículo se crea en el mismo paso (RF-05).
@ApplicationScoped
public class RegisterEntryService implements RegisterEntryUseCase {

    private final ParkingZoneRepository zoneRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingRecordRepository recordRepository;
    private final ParkingRulesService rules = new ParkingRulesService();

    public RegisterEntryService(ParkingZoneRepository zoneRepository,
                                VehicleRepository vehicleRepository,
                                ParkingRecordRepository recordRepository) {
        this.zoneRepository = zoneRepository;
        this.vehicleRepository = vehicleRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    @Transactional
    public ParkingRecord registerEntry(EntryCommand command) {
        if (command == null) {
            throw new BusinessRuleException("Los datos de la entrada son obligatorios");
        }
        if (command.zoneId() == null) {
            throw new BusinessRuleException("Debes seleccionar una zona");
        }

        String plate = rules.normalizePlate(command.plate());
        ParkingZone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> new ResourceNotFoundException("La zona seleccionada no existe"));

        // Vehículo existente o nuevo (todavía sin guardar)
        Vehicle vehicle = vehicleRepository.findByPlate(plate).orElse(null);
        boolean isNew = vehicle == null;
        if (isNew) {
            vehicle = new Vehicle();
            vehicle.setPlate(plate);
            vehicle.setVehicleType(command.vehicleType());
            vehicle.setOwnerName(command.ownerName());
            vehicle.setOwnerType(command.ownerType());
            rules.validateVehicle(vehicle);
        }

        // RN-02, RN-03 y RN-04 las valida el dominio
        boolean alreadyInside = !isNew
                && recordRepository.findActiveByVehicleId(vehicle.getId()).isPresent();
        int vehiclesInside = recordRepository.countActiveByZoneId(zone.getId());
        ParkingRecord record = rules.registerEntry(vehicle, zone, alreadyInside, vehiclesInside);

        // El vehículo nuevo se guarda solo si la entrada fue válida
        if (isNew) {
            record.setVehicle(vehicleRepository.save(vehicle));
        }
        return recordRepository.save(record);
    }
}
