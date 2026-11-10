package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.OwnerType;
import usta.parking.domain.model.Vehicle;
import usta.parking.domain.model.VehicleType;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.SelectOptionDto;
import usta.parking.infrastructure.adapter.in.web.dto.VehicleFormDto;
import usta.parking.infrastructure.adapter.in.web.dto.VehicleRowDto;

import java.util.List;

// Convierte el formulario de vehículos en el modelo del dominio y arma las filas de la tabla
@ApplicationScoped
public class VehiclesWebMapper {

    private final EnumLabelMapper labels;

    public VehiclesWebMapper(EnumLabelMapper labels) {
        this.labels = labels;
    }

    // Formulario vacío (vehículo nuevo)
    public VehicleFormDto emptyForm() {
        return new VehicleFormDto(null, "", "", "", "");
    }

    // Formulario con los datos de un vehículo existente (modo edición)
    public VehicleFormDto toForm(Vehicle vehicle) {
        return new VehicleFormDto(
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getVehicleType() == null ? "" : vehicle.getVehicleType().name(),
                vehicle.getOwnerName() == null ? "" : vehicle.getOwnerName(),
                vehicle.getOwnerType() == null ? "" : vehicle.getOwnerType().name());
    }

    // Guarda lo que llegó del formulario (todo como texto) para poder volver a mostrarlo
    public VehicleFormDto toForm(Long id, String plate, String vehicleType,
                                 String ownerName, String ownerType) {
        return new VehicleFormDto(
                id,
                plate == null ? "" : plate.trim(),
                vehicleType == null ? "" : vehicleType,
                ownerName == null ? "" : ownerName.trim(),
                ownerType == null ? "" : ownerType);
    }

    // Convierte el formulario en un vehículo del dominio.
    // Si un valor no es válido queda en nulo y el caso de uso avisa qué dato falta.
    public Vehicle toDomain(VehicleFormDto form) {
        return Vehicle.builder()
                .id(form.id())
                .plate(form.plate())
                .vehicleType(parseVehicleType(form.vehicleType()))
                .ownerName(form.ownerName())
                .ownerType(parseOwnerType(form.ownerType()))
                .build();
    }

    public VehicleRowDto toRow(Vehicle vehicle, String editHref) {
        return new VehicleRowDto(
                vehicle.getId(),
                vehicle.getPlate(),
                labels.vehicleTypeLabel(vehicle.getVehicleType()),
                labels.vehicleTypeIcon(vehicle.getVehicleType()),
                vehicle.getOwnerName(),
                labels.ownerTypeLabel(vehicle.getOwnerType()),
                labels.ownerTypeCssClass(vehicle.getOwnerType()),
                editHref);
    }

    public List<SelectOptionDto> vehicleTypeOptions() {
        return labels.vehicleTypeOptions();
    }

    public List<SelectOptionDto> ownerTypeOptions() {
        return labels.ownerTypeOptions();
    }

    public String formTitle(VehicleFormDto form) {
        return form.id() == null ? "Registrar vehículo" : "Editar vehículo";
    }

    public String formAction(VehicleFormDto form) {
        return form.id() == null ? "/vehicles" : "/vehicles/" + form.id();
    }

    public String submitLabel(VehicleFormDto form) {
        return form.id() == null ? "Guardar vehículo" : "Guardar cambios";
    }

    // Mensaje de éxito después de guardar (action: "created" o "updated")
    public AlertDto toSuccessAlert(String plate, String action) {
        String verb = "updated".equals(action) ? "actualizado" : "registrado";
        return AlertDto.success("Vehículo " + plate + " " + verb + " correctamente");
    }

    private VehicleType parseVehicleType(String value) {
        try {
            return VehicleType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private OwnerType parseOwnerType(String value) {
        try {
            return OwnerType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
