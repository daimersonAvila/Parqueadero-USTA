package usta.parking.application.port.out;

import usta.parking.domain.model.ParkingZone;

import java.util.List;
import java.util.Optional;

// Puerto de salida: persistencia de zonas del parqueadero
public interface ParkingZoneRepository {

    // Crea la zona (sin id) o actualiza una existente (con id)
    ParkingZone save(ParkingZone zone);

    Optional<ParkingZone> findById(Long id);

    List<ParkingZone> findAll();

    // Solo las zonas abiertas (active = true)
    List<ParkingZone> findAllActive();
}
