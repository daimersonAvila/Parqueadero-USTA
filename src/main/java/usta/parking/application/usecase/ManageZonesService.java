package usta.parking.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import usta.parking.application.exception.ResourceNotFoundException;
import usta.parking.application.port.in.ManageZonesUseCase;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.application.port.out.ParkingZoneRepository;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.service.ParkingRulesService;

import java.util.List;
import java.util.Objects;

// Caso de uso: gestionar las zonas del parqueadero (RF-13 y RF-15)
@ApplicationScoped
public class ManageZonesService implements ManageZonesUseCase {

    private final ParkingZoneRepository zoneRepository;
    private final ParkingRecordRepository recordRepository;
    private final ParkingRulesService rules = new ParkingRulesService();

    public ManageZonesService(ParkingZoneRepository zoneRepository,
                              ParkingRecordRepository recordRepository) {
        this.zoneRepository = zoneRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    @Transactional
    public ParkingZone create(ParkingZone zone) {
        rules.validateZone(zone); // RN-07
        requireUniqueName(zone.getName(), null);
        zone.setId(null);
        zone.setName(zone.getName().trim());
        return zoneRepository.save(zone);
    }

    @Override
    @Transactional
    public ParkingZone update(Long id, ParkingZone zone) {
        ParkingZone current = findById(id);
        rules.validateZone(zone);

        // RN-07: no se baja la capacidad por debajo de los vehículos que hay adentro
        int vehiclesInside = recordRepository.countActiveByZoneId(id);
        rules.validateCapacityChange(zone.getCapacity(), vehiclesInside);
        if (zone.getVehicleType() != current.getVehicleType() && vehiclesInside > 0) {
            throw new BusinessRuleException("No se puede cambiar el tipo de vehículo de la zona: hay "
                    + vehiclesInside + " vehículos adentro");
        }
        requireUniqueName(zone.getName(), id);

        current.setName(zone.getName().trim());
        current.setVehicleType(zone.getVehicleType());
        current.setCapacity(zone.getCapacity());
        return zoneRepository.save(current);
    }

    // Abrir o cerrar una zona; los vehículos que ya están adentro pueden salir normalmente
    @Override
    @Transactional
    public ParkingZone setActive(Long id, boolean active) {
        ParkingZone current = findById(id);
        current.setActive(active);
        return zoneRepository.save(current);
    }

    @Override
    public ParkingZone findById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("La zona no existe");
        }
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La zona no existe"));
    }

    @Override
    public List<ParkingZone> findAll() {
        return zoneRepository.findAll();
    }

    // No puede haber dos zonas con el mismo nombre (ignoreId = la zona que se está editando)
    private void requireUniqueName(String name, Long ignoreId) {
        String wanted = name.trim();
        boolean exists = zoneRepository.findAll().stream()
                .filter(other -> !Objects.equals(other.getId(), ignoreId))
                .anyMatch(other -> other.getName() != null && other.getName().trim().equalsIgnoreCase(wanted));
        if (exists) {
            throw new BusinessRuleException("Ya existe una zona llamada " + wanted);
        }
    }
}
