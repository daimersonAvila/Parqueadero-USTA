package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

// Datos de la pantalla "Registrar entrada"
@TemplateData
public record EntryPageDto(List<ZoneCardDto> zones,
                           EntryFormDto form,
                           List<SelectOptionDto> vehicleTypes,
                           List<SelectOptionDto> ownerTypes,
                           AlertDto alert) {   // puede ser nulo (sin mensaje)
}
