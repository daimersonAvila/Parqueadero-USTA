package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.port.in.RegisterEntryUseCase.EntryCommand;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.OwnerType;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.VehicleType;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.EntryFormDto;
import usta.parking.infrastructure.adapter.in.web.dto.EntryPageDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneCardDto;

import java.util.List;

// Convierte el formulario de entrada en el comando del caso de uso y arma la vista
@ApplicationScoped
public class EntryWebMapper {

    private final EnumLabelMapper labels;
    private final DisplayFormatter formatter;

    public EntryWebMapper(EnumLabelMapper labels, DisplayFormatter formatter) {
        this.labels = labels;
        this.formatter = formatter;
    }

    // Formulario vacío (al abrir la pantalla o después de registrar una entrada)
    public EntryFormDto emptyForm() {
        return new EntryFormDto("", null, false, "", "", "");
    }

    // Guarda lo que llegó del formulario (todo como texto) para poder volver a mostrarlo
    public EntryFormDto toForm(String plate, String zoneId, String newVehicle,
                               String vehicleType, String ownerName, String ownerType) {
        return new EntryFormDto(
                plate == null ? "" : plate.trim(),
                parseId(zoneId),
                newVehicle != null,   // el interruptor solo se envía cuando está activado
                vehicleType == null ? "" : vehicleType,
                ownerName == null ? "" : ownerName.trim(),
                ownerType == null ? "" : ownerType);
    }

    // Convierte el formulario en el comando del caso de uso.
    // Los datos del vehículo solo se envían cuando es un vehículo nuevo.
    public EntryCommand toCommand(EntryFormDto form) {
        if (form.zoneId() == null) {
            throw new BusinessRuleException("Selecciona una zona");
        }
        if (!form.newVehicle()) {
            return new EntryCommand(form.plate(), form.zoneId(), null, null, null);
        }
        String ownerName = form.ownerName().isBlank() ? null : form.ownerName();
        return new EntryCommand(form.plate(), form.zoneId(),
                parseVehicleType(form.vehicleType()), ownerName, parseOwnerType(form.ownerType()));
    }

    public EntryPageDto toPage(List<ZoneCardDto> zones, EntryFormDto form, AlertDto alert) {
        return new EntryPageDto(zones, form, labels.vehicleTypeOptions(), labels.ownerTypeOptions(), alert);
    }

    // Mensaje de éxito: "Entrada registrada: ABC123 en Zona A - Carros a las 07:42"
    public AlertDto toSuccessAlert(ParkingRecord record) {
        return AlertDto.success("Entrada registrada: " + record.getVehicle().getPlate()
                + " en " + record.getZone().getName()
                + " a las " + formatter.hour(record.getEntryTime()));
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Si el valor no es válido devuelve nulo y el caso de uso avisa que falta el dato
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
