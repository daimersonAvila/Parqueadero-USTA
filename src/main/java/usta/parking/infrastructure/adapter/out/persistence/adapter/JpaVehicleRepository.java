package usta.parking.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import usta.parking.application.port.out.VehicleRepository;
import usta.parking.domain.model.Vehicle;
import usta.parking.infrastructure.adapter.out.persistence.entity.VehicleEntity;
import usta.parking.infrastructure.adapter.out.persistence.mapper.VehicleEntityMapper;
import usta.parking.infrastructure.adapter.out.persistence.repository.VehicleJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: implementa el puerto VehicleRepository con JPA. */
@ApplicationScoped
public class JpaVehicleRepository implements VehicleRepository {

    @Inject
    VehicleJpaRepository jpa;

    @Inject
    VehicleEntityMapper mapper;

    @Override
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity saved = jpa.getEntityManager().merge(mapper.toEntity(vehicle));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpa.findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return jpa.findByPlate(plate).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPlate(String plate) {
        return jpa.existsByPlate(plate);
    }

    @Override
    public List<Vehicle> findAll() {
        return jpa.listAll().stream().map(mapper::toDomain).toList();
    }
}
