package usta.parking.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.exception.ResourceNotFoundException;
import usta.parking.application.port.in.CheckAvailabilityUseCase;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.application.port.out.ParkingZoneRepository;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.service.AvailabilityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Caso de uso: consultar la disponibilidad (RF-01 a RF-03).
// Junta los datos de los puertos de salida y deja los cálculos al dominio (RN-01 y RN-08).
@ApplicationScoped
public class CheckAvailabilityService implements CheckAvailabilityUseCase {

    private final ParkingZoneRepository zoneRepository;
    private final ParkingRecordRepository recordRepository;
    private final AvailabilityService availability = new AvailabilityService();

    public CheckAvailabilityService(ParkingZoneRepository zoneRepository,
                                    ParkingRecordRepository recordRepository) {
        this.zoneRepository = zoneRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    public AvailabilitySummary getSummary() {
        // Una sola consulta para saber cuántos vehículos hay adentro en cada zona
        Map<Long, Integer> insideByZone = recordRepository.countActiveGroupedByZone();

        List<ZoneAvailability> zones = new ArrayList<>();
        int totalFree = 0;
        int totalCapacity = 0;
        for (ParkingZone zone : zoneRepository.findAllActive()) {
            ZoneAvailability item = toAvailability(zone, insideByZone.getOrDefault(zone.getId(), 0));
            zones.add(item);
            totalFree += item.freeSpaces();
            totalCapacity += zone.getCapacity();
        }
        return new AvailabilitySummary(zones, totalFree, totalCapacity);
    }

    @Override
    public ZoneAvailability getZone(Long zoneId) {
        if (zoneId == null) {
            throw new ResourceNotFoundException("La zona no existe");
        }
        ParkingZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("La zona no existe"));
        return toAvailability(zone, recordRepository.countActiveByZoneId(zoneId));
    }

    private ZoneAvailability toAvailability(ParkingZone zone, int vehiclesInside) {
        return new ZoneAvailability(
                zone,
                vehiclesInside,
                availability.freeSpaces(zone, vehiclesInside),
                availability.occupancyPercent(zone, vehiclesInside),
                availability.statusOf(zone, vehiclesInside));
    }
}
