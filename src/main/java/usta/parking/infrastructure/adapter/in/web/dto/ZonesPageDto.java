package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

// Datos de la pantalla "Zonas"
@TemplateData
public record ZonesPageDto(List<ZoneRowDto> rows,
                           int totalZones,
                           String totalLabel,          // "zona" o "zonas"
                           ZoneFormDto form,
                           boolean editing,            // true = el formulario edita una zona
                           String formTitle,           // "Nueva zona" o "Editar zona"
                           String formAction,          // dirección a la que se envía el formulario
                           String submitLabel,         // texto del botón de guardar
                           List<SelectOptionDto> vehicleTypes,
                           AlertDto alert) {           // puede ser nulo (sin mensaje)
}
