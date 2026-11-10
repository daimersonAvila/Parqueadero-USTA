package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Valores del formulario de zonas.
// Se guardan tal como los escribió la persona para volver a mostrarlos si hay un error.
@TemplateData
public record ZoneFormDto(Long id,              // nulo = zona nueva; con valor = se está editando
                          String name,
                          String vehicleType,   // nombre del enum (CAR, MOTORCYCLE) o ""
                          String capacity,      // texto, para devolver lo que escribió la persona
                          boolean active) {     // solo se usa al crear; al editar se cambia en la tabla
}
