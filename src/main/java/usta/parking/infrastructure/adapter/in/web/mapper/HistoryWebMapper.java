package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.model.RecordStatus;
import usta.parking.infrastructure.adapter.in.web.dto.HistoryRowDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneOptionDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

// Convierte los registros del historial en filas de la tabla
@ApplicationScoped
public class HistoryWebMapper {

    // En el historial siempre se muestra la fecha, porque los registros son de varios días
    private static final DateTimeFormatter DAY_AND_HOUR = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final DisplayFormatter formatter;

    public HistoryWebMapper(DisplayFormatter formatter) {
        this.formatter = formatter;
    }

    // minutesStayed lo calcula el caso de uso (RN-06)
    public HistoryRowDto toRow(ParkingRecord record, long minutesStayed) {
        boolean inside = record.getStatus() == RecordStatus.ACTIVE;
        return new HistoryRowDto(
                record.getVehicle().getPlate(),
                record.getVehicle().getOwnerName(),
                record.getZone().getName(),
                dayAndHour(record.getEntryTime()),
                dayAndHour(record.getExitTime()),
                formatter.duration(minutesStayed),
                inside ? "ADENTRO" : "FINALIZADO",
                inside ? "rec-active" : "rec-finished");
    }

    public List<ZoneOptionDto> toZoneOptions(List<ParkingZone> zones) {
        return zones.stream()
                .sorted(Comparator.comparing(ParkingZone::getName, String.CASE_INSENSITIVE_ORDER))
                .map(zone -> new ZoneOptionDto(zone.getId(), zone.getName()))
                .toList();
    }

    private String dayAndHour(LocalDateTime time) {
        return time == null ? "-" : time.format(DAY_AND_HOUR);
    }
}
