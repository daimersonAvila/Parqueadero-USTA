package usta.parking.domain.service;

import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.model.ZoneStatus;

// Servicio que calcula la disponibilidad de una zona (RN-01 y RN-08).
// El dominio no consulta la base de datos: recibe la cantidad de vehículos que hay adentro.
public class AvailabilityService {

    // Una zona tiene "pocos cupos" cuando los libres son el 10% o menos de la capacidad
    private static final int FEW_SPACES_PERCENT = 10;

    // RN-01: cupos libres = capacidad - vehículos adentro (nunca negativo)
    // Una zona inactiva (cerrada) no ofrece cupos
    public int freeSpaces(ParkingZone zone, int vehiclesInside) {
        requireZone(zone);
        if (vehiclesInside < 0) {
            throw new BusinessRuleException("La cantidad de vehículos adentro no puede ser negativa");
        }
        if (!zone.isActive()) {
            return 0;
        }
        return Math.max(0, zone.getCapacity() - vehiclesInside);
    }

    // RN-08: estado de la zona según sus cupos libres
    public ZoneStatus statusOf(ParkingZone zone, int vehiclesInside) {
        int free = freeSpaces(zone, vehiclesInside);
        if (free == 0) {
            return ZoneStatus.FULL;
        }
        if (free * 100 <= zone.getCapacity() * FEW_SPACES_PERCENT) {
            return ZoneStatus.FEW_SPACES;
        }
        return ZoneStatus.AVAILABLE;
    }

    // Porcentaje de ocupación (0 a 100) para la barra de la vista
    public int occupancyPercent(ParkingZone zone, int vehiclesInside) {
        requireZone(zone);
        if (zone.getCapacity() <= 0) {
            return 0;
        }
        int occupied = Math.min(vehiclesInside, zone.getCapacity());
        return occupied * 100 / zone.getCapacity();
    }

    // Suma los cupos libres de varias zonas (total general de la página principal)
    public int totalFree(java.util.Map<ParkingZone, Integer> insideByZone) {
        int total = 0;
        for (var entry : insideByZone.entrySet()) {
            total += freeSpaces(entry.getKey(), entry.getValue());
        }
        return total;
    }

    private void requireZone(ParkingZone zone) {
        if (zone == null) {
            throw new BusinessRuleException("La zona no existe");
        }
    }
}
