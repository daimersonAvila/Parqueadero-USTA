package usta.parking.infrastructure.adapter.out.persistence.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.Vehicle;
import usta.parking.infrastructure.adapter.out.persistence.entity.VehicleEntity;

/** Convierte entre Vehicle (dominio) y VehicleEntity (JPA). */
@ApplicationScoped
public class VehicleEntityMapper {

    public Vehicle toDomain(VehicleEntity e) {
        if (e == null) return null;
        return Vehicle.builder()
                .id(e.getId())
                .plate(e.getPlate())
                .vehicleType(e.getVehicleType())
                .ownerName(e.getOwnerName())
                .ownerType(e.getOwnerType())
                .build();
    }

    public VehicleEntity toEntity(Vehicle d) {
        VehicleEntity e = new VehicleEntity();
        e.setId(d.getId());
        e.setPlate(d.getPlate());
        e.setVehicleType(d.getVehicleType());
        e.setOwnerName(d.getOwnerName());
        e.setOwnerType(d.getOwnerType());
        return e;
    }
}
