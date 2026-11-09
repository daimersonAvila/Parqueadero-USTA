package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Valores del formulario "Registrar entrada".
// Se guardan tal como los escribió la persona para volver a mostrarlos si hay un error.
@TemplateData
public record EntryFormDto(String plate,
                           Long zoneId,
                           boolean newVehicle,
                           String vehicleType,   // nombre del enum (CAR, MOTORCYCLE) o ""
                           String ownerName,
                           String ownerType) {   // nombre del enum (STUDENT, ...) o ""
}
