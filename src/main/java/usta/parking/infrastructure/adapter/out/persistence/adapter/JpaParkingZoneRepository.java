package usta.parking.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import usta.parking.application.port.out.ParkingZoneRepository;
import usta.parking.domain.model.ParkingZone;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingZoneEntity;
import usta.parking.infrastructure.adapter.out.persistence.mapper.ParkingZoneEntityMapper;
import usta.parking.infrastructure.adapter.out.persistence.repository.ParkingZoneJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: implementa el puerto ParkingZoneRepository con JPA. */
@ApplicationScoped
public class JpaParkingZoneRepository implements ParkingZoneRepository {

    @Inject
    ParkingZoneJpaRepository jpa;

    @Inject
    ParkingZoneEntityMapper mapper;

    @Override
    @Transactional
    public ParkingZone save(ParkingZone zone) {
        // merge crea la zona si no tiene id y la actualiza si ya lo tiene
        ParkingZoneEntity saved = jpa.getEntityManager().merge(mapper.toEntity(zone));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ParkingZone> findById(Long id) {
        return jpa.findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public List<ParkingZone> findAll() {
        return jpa.listAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ParkingZone> findAllActive() {
        return jpa.findAllActive().stream().map(mapper::toDomain).toList();
    }
}
