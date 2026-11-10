package usta.parking.infrastructure.adapter.out.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.infrastructure.adapter.out.persistence.entity.VehicleEntity;

import java.util.Optional;

/** Acceso JPA/Panache a los vehículos. */
@ApplicationScoped
public class VehicleJpaRepository implements PanacheRepository<VehicleEntity> {

    public Optional<VehicleEntity> findByPlate(String plate) {
        return find("plate", plate).firstResultOptional();
    }

    public boolean existsByPlate(String plate) {
        return count("plate", plate) > 0;
    }
}
