package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.model.VehicleType;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.SelectOptionDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneFormDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneRowDto;

import java.util.List;

// Convierte el formulario de zonas en el modelo del dominio y arma las filas de la tabla
@ApplicationScoped
public class ZonesWebMapper {

    private final EnumLabelMapper labels;

    public ZonesWebMapper(EnumLabelMapper labels) {
        this.labels = labels;
    }

    // Formulario vacío (zona nueva, activa por defecto)
    public ZoneFormDto emptyForm() {
        return new ZoneFormDto(null, "", "", "", true);
    }

    // Formulario con los datos de una zona existente (modo edición)
    public ZoneFormDto toForm(ParkingZone zone) {
        return new ZoneFormDto(
                zone.getId(),
                zone.getName() == null ? "" : zone.getName(),
                zone.getVehicleType() == null ? "" : zone.getVehicleType().name(),
                String.valueOf(zone.getCapacity()),
                zone.isActive());
    }

    // Guarda lo que llegó del formulario (todo como texto) para poder volver a mostrarlo
    public ZoneFormDto toForm(Long id, String name, String vehicleType, String capacity, boolean active) {
        return new ZoneFormDto(
                id,
                name == null ? "" : name.trim(),
                vehicleType == null ? "" : vehicleType,
                capacity == null ? "" : capacity.trim(),
                active);
    }

    // Convierte el formulario en una zona del dominio.
    // Si un valor no es válido queda en nulo o 0 y el caso de uso avisa qué dato falta.
    public ParkingZone toDomain(ZoneFormDto form) {
        return ParkingZone.builder()
                .id(form.id())
                .name(form.name())
                .vehicleType(parseVehicleType(form.vehicleType()))
                .capacity(parseCapacity(form.capacity()))
                .active(form.active())
                .build();
    }

    // occupied lo calcula el resource contando los vehículos adentro de la zona
    public ZoneRowDto toRow(ParkingZone zone, int occupied, String editHref) {
        return new ZoneRowDto(
                zone.getId(),
                zone.getName(),
                labels.vehicleTypeLabel(zone.getVehicleType()),
                labels.vehicleTypeIcon(zone.getVehicleType()),
                zone.getCapacity(),
                occupied,
                zone.isActive(),
                zone.isActive() ? "Activa" : "Cerrada",
                !zone.isActive(),
                editHref);
    }

    public List<SelectOptionDto> vehicleTypeOptions() {
        return labels.vehicleTypeOptions();
    }

    public String formTitle(ZoneFormDto form) {
        return form.id() == null ? "Nueva zona" : "Editar zona";
    }

    public String formAction(ZoneFormDto form) {
        return form.id() == null ? "/zones" : "/zones/" + form.id();
    }

    public String submitLabel(ZoneFormDto form) {
        return form.id() == null ? "Guardar zona" : "Guardar cambios";
    }

    // Mensaje de éxito (action: created, updated, opened o closed)
    public AlertDto toSuccessAlert(String zoneName, String action) {
        String text = switch (action == null ? "" : action) {
            case "updated" -> "Zona actualizada correctamente: ";
            case "opened" -> "Zona abierta: ";
            case "closed" -> "Zona cerrada: ";
            default -> "Zona guardada correctamente: ";
        };
        return AlertDto.success(text + zoneName);
    }

    private VehicleType parseVehicleType(String value) {
        try {
            return VehicleType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    // Si no es un número devuelve 0 y el caso de uso responde "La capacidad debe ser mayor que 0"
    private int parseCapacity(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }
}
