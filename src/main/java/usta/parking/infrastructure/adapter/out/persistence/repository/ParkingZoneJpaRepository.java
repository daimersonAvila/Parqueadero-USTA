package usta.parking.infrastructure.adapter.out.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingZoneEntity;

import java.util.List;

/** Acceso JPA/Panache a las zonas. */
@ApplicationScoped
public class ParkingZoneJpaRepository implements PanacheRepository<ParkingZoneEntity> {

    /** Solo las zonas abiertas (active = true). */
    public List<ParkingZoneEntity> findAllActive() {
        return list("active", true);
    }
}
