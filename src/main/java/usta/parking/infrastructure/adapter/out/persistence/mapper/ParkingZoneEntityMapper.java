package usta.parking.infrastructure.adapter.out.persistence.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.ParkingZone;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingZoneEntity;

/** Convierte entre ParkingZone (dominio) y ParkingZoneEntity (JPA). */
@ApplicationScoped
public class ParkingZoneEntityMapper {

    public ParkingZone toDomain(ParkingZoneEntity e) {
        if (e == null) return null;
        return ParkingZone.builder()
                .id(e.getId())
                .name(e.getName())
                .vehicleType(e.getVehicleType())
                .capacity(e.getCapacity())
                .active(e.isActive())
                .build();
    }

    public ParkingZoneEntity toEntity(ParkingZone d) {
        ParkingZoneEntity e = new ParkingZoneEntity();
        e.setId(d.getId());
        e.setName(d.getName());
        e.setVehicleType(d.getVehicleType());
        e.setCapacity(d.getCapacity());
        e.setActive(d.isActive());
        return e;
    }
}
