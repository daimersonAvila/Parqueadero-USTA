package usta.parking.infrastructure.adapter.out.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.RecordStatus;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingRecordEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Acceso JPA/Panache a los registros de entrada y salida. */
@ApplicationScoped
public class ParkingRecordJpaRepository implements PanacheRepository<ParkingRecordEntity> {

    /** Vehículos que están adentro ahora (los más recientes primero). */
    public List<ParkingRecordEntity> findActive() {
        return list("status = ?1 order by entryTime desc", RecordStatus.ACTIVE);
    }

    public Optional<ParkingRecordEntity> findActiveByVehicleId(Long vehicleId) {
        return find("vehicle.id = ?1 and status = ?2", vehicleId, RecordStatus.ACTIVE)
                .firstResultOptional();
    }

    public Optional<ParkingRecordEntity> findActiveByPlate(String plate) {
        return find("vehicle.plate = ?1 and status = ?2", plate, RecordStatus.ACTIVE)
                .firstResultOptional();
    }

    public long countActiveByZoneId(Long zoneId) {
        return count("zone.id = ?1 and status = ?2", zoneId, RecordStatus.ACTIVE);
    }

    /** Vehículos adentro por zona en una sola consulta: clave = id de la zona. */
    public Map<Long, Integer> countActiveGroupedByZone() {
        List<Object[]> rows = getEntityManager()
                .createQuery("select r.zone.id, count(r) from ParkingRecordEntity r "
                        + "where r.status = :status group by r.zone.id", Object[].class)
                .setParameter("status", RecordStatus.ACTIVE)
                .getResultList();
        Map<Long, Integer> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return result;
    }

    /** Historial con entrada entre dos fechas; zoneId nulo = todas las zonas. */
    public List<ParkingRecordEntity> findByEntryBetween(LocalDateTime from, LocalDateTime to, Long zoneId) {
        if (zoneId == null) {
            return list("entryTime between ?1 and ?2 order by entryTime desc", from, to);
        }
        return list("entryTime between ?1 and ?2 and zone.id = ?3 order by entryTime desc",
                from, to, zoneId);
    }
}
