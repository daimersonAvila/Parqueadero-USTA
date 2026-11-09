package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.ParkingZone;
import usta.parking.infrastructure.adapter.in.web.dto.InsideRowDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneOptionDto;

import java.util.List;

// Convierte los registros que están adentro en filas de la tabla
@ApplicationScoped
public class InsideWebMapper {

    private final EnumLabelMapper labels;
    private final DisplayFormatter formatter;

    public InsideWebMapper(EnumLabelMapper labels, DisplayFormatter formatter) {
        this.labels = labels;
        this.formatter = formatter;
    }

    // minutesStayed lo calcula el caso de uso (RN-06)
    public InsideRowDto toRow(ParkingRecord record, long minutesStayed) {
        return new InsideRowDto(
                record.getId(),
                record.getVehicle().getPlate(),
                labels.vehicleTypeLabel(record.getVehicle().getVehicleType()),
                labels.vehicleTypeIcon(record.getVehicle().getVehicleType()),
                record.getVehicle().getOwnerName(),
                labels.ownerTypeLabel(record.getVehicle().getOwnerType()),
                labels.ownerTypeCssClass(record.getVehicle().getOwnerType()),
                record.getZone().getName(),
                formatter.entryTime(record.getEntryTime()),
                formatter.duration(minutesStayed));
    }

    public List<ZoneOptionDto> toZoneOptions(List<ParkingZone> zones) {
        return zones.stream()
                .map(zone -> new ZoneOptionDto(zone.getId(), zone.getName()))
                .toList();
    }
}
