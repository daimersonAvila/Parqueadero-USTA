package usta.parking.application.port.in;

import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.model.ZoneStatus;

import java.util.List;

// Puerto de entrada: consultar la disponibilidad del parqueadero (RF-01 a RF-03)
public interface CheckAvailabilityUseCase {

    // Disponibilidad de una zona (datos listos para mostrar en la vista)
    record ZoneAvailability(ParkingZone zone, int vehiclesInside, int freeSpaces,
                            int occupancyPercent, ZoneStatus status) {
    }

    // Disponibilidad de todas las zonas más los totales generales
    record AvailabilitySummary(List<ZoneAvailability> zones, int totalFree, int totalCapacity) {
    }

    // Todas las zonas activas con sus cupos y el total general
    AvailabilitySummary getSummary();

    // Una sola zona (lanza ResourceNotFoundException si no existe)
    ZoneAvailability getZone(Long zoneId);
}
