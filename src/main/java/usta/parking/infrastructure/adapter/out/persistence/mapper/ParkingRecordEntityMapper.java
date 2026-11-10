package usta.parking.infrastructure.adapter.out.persistence.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingRecordEntity;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingZoneEntity;
import usta.parking.infrastructure.adapter.out.persistence.entity.VehicleEntity;

/** Convierte entre ParkingRecord (dominio) y ParkingRecordEntity (JPA). */
@ApplicationScoped
public class ParkingRecordEntityMapper {

    @Inject
    VehicleEntityMapper vehicleMapper;

    @Inject
    ParkingZoneEntityMapper zoneMapper;

    public ParkingRecord toDomain(ParkingRecordEntity e) {
        if (e == null) return null;
        return ParkingRecord.builder()
                .id(e.getId())
                .vehicle(vehicleMapper.toDomain(e.getVehicle()))
                .zone(zoneMapper.toDomain(e.getZone()))
                .entryTime(e.getEntryTime())
                .exitTime(e.getExitTime())
                .status(e.getStatus())
                .build();
    }

    /** Recibe las entidades vehículo y zona ya cargadas para armar bien las llaves foráneas. */
    public ParkingRecordEntity toEntity(ParkingRecord d, VehicleEntity vehicle, ParkingZoneEntity zone) {
        ParkingRecordEntity e = new ParkingRecordEntity();
        e.setId(d.getId());
        e.setVehicle(vehicle);
        e.setZone(zone);
        e.setEntryTime(d.getEntryTime());
        e.setExitTime(d.getExitTime());
        e.setStatus(d.getStatus());
        return e;
    }
}
