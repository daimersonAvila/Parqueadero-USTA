package usta.parking.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingRecordEntity;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingZoneEntity;
import usta.parking.infrastructure.adapter.out.persistence.entity.VehicleEntity;
import usta.parking.infrastructure.adapter.out.persistence.mapper.ParkingRecordEntityMapper;
import usta.parking.infrastructure.adapter.out.persistence.repository.ParkingRecordJpaRepository;
import usta.parking.infrastructure.adapter.out.persistence.repository.ParkingZoneJpaRepository;
import usta.parking.infrastructure.adapter.out.persistence.repository.VehicleJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Adaptador de salida: implementa el puerto ParkingRecordRepository con JPA. */
@ApplicationScoped
public class JpaParkingRecordRepository implements ParkingRecordRepository {

    @Inject
    ParkingRecordJpaRepository jpa;

    @Inject
    VehicleJpaRepository vehicleJpa;

    @Inject
    ParkingZoneJpaRepository zoneJpa;

    @Inject
    ParkingRecordEntityMapper mapper;

    @Override
    @Transactional
    public ParkingRecord save(ParkingRecord record) {
        // Se cargan las entidades gestionadas para que las llaves foráneas queden bien
        VehicleEntity vehicle = vehicleJpa.findById(record.getVehicle().getId());
        ParkingZoneEntity zone = zoneJpa.findById(record.getZone().getId());
        ParkingRecordEntity saved = jpa.getEntityManager()
                .merge(mapper.toEntity(record, vehicle, zone));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ParkingRecord> findById(Long id) {
        return jpa.findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public List<ParkingRecord> findAll() {
        return jpa.listAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ParkingRecord> findActive() {
        return jpa.findActive().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<ParkingRecord> findActiveByVehicleId(Long vehicleId) {
        return jpa.findActiveByVehicleId(vehicleId).map(mapper::toDomain);
    }

    @Override
    public Optional<ParkingRecord> findActiveByPlate(String plate) {
        return jpa.findActiveByPlate(plate).map(mapper::toDomain);
    }

    @Override
    public int countActiveByZoneId(Long zoneId) {
        return (int) jpa.countActiveByZoneId(zoneId);
    }

    @Override
    public Map<Long, Integer> countActiveGroupedByZone() {
        return jpa.countActiveGroupedByZone();
    }

    @Override
    public List<ParkingRecord> findByEntryBetween(LocalDateTime from, LocalDateTime to, Long zoneId) {
        return jpa.findByEntryBetween(from, to, zoneId).stream().map(mapper::toDomain).toList();
    }
}
