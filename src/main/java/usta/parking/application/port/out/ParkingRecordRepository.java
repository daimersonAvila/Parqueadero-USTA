package usta.parking.application.port.out;

import usta.parking.domain.model.ParkingRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Puerto de salida: persistencia de los registros de entrada y salida
public interface ParkingRecordRepository {

    // Guarda la entrada (sin id) o actualiza el registro al registrar la salida (con id)
    ParkingRecord save(ParkingRecord record);

    Optional<ParkingRecord> findById(Long id);

    List<ParkingRecord> findAll();

    // Registros ACTIVE: los vehículos que están adentro ahora (más recientes primero)
    List<ParkingRecord> findActive();

    // Registro ACTIVE de un vehículo, si está adentro (RN-02)
    Optional<ParkingRecord> findActiveByVehicleId(Long vehicleId);

    // Registro ACTIVE buscando por placa (para registrar la salida)
    Optional<ParkingRecord> findActiveByPlate(String plate);

    // Cuántos vehículos hay adentro en una zona (RN-01)
    int countActiveByZoneId(Long zoneId);

    // Vehículos adentro por zona en una sola consulta: clave = id de la zona
    // Las zonas sin vehículos pueden no aparecer en el mapa (se toman como 0)
    Map<Long, Integer> countActiveGroupedByZone();

    // Historial con entrada entre dos fechas; zoneId es opcional (null = todas las zonas)
    List<ParkingRecord> findByEntryBetween(LocalDateTime from, LocalDateTime to, Long zoneId);
}
