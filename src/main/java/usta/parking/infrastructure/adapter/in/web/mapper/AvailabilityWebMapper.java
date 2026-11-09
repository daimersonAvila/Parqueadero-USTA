package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.port.in.CheckAvailabilityUseCase.AvailabilitySummary;
import usta.parking.application.port.in.CheckAvailabilityUseCase.ZoneAvailability;
import usta.parking.domain.model.ParkingZone;
import usta.parking.infrastructure.adapter.in.web.dto.AvailabilityPageDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneCardDto;

import java.time.LocalDateTime;
import java.util.List;

// Convierte la disponibilidad del caso de uso en datos para las vistas
@ApplicationScoped
public class AvailabilityWebMapper {

    private final EnumLabelMapper labels;
    private final DisplayFormatter formatter;

    public AvailabilityWebMapper(EnumLabelMapper labels, DisplayFormatter formatter) {
        this.labels = labels;
        this.formatter = formatter;
    }

    public ZoneCardDto toCard(ZoneAvailability availability) {
        ParkingZone zone = availability.zone();
        return new ZoneCardDto(
                zone.getId(),
                zone.getName(),
                zone.getVehicleType().name(),
                labels.vehicleTypeLabel(zone.getVehicleType()),
                labels.vehicleTypeIcon(zone.getVehicleType()),
                availability.freeSpaces(),
                formatter.freeSpacesLabel(availability.freeSpaces()),
                availability.vehiclesInside(),
                zone.getCapacity(),
                availability.occupancyPercent(),
                labels.statusLabel(availability.status()),
                labels.statusCssClass(availability.status()));
    }

    public List<ZoneCardDto> toCards(AvailabilitySummary summary) {
        return summary.zones().stream().map(this::toCard).toList();
    }

    public AvailabilityPageDto toPage(AvailabilitySummary summary) {
        LocalDateTime now = LocalDateTime.now();
        return new AvailabilityPageDto(
                toCards(summary),
                summary.totalFree(),
                summary.totalCapacity(),
                summary.zones().size(),
                formatter.hourWithSeconds(now),
                System.currentTimeMillis());
    }
}
