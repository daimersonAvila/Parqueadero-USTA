package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Una fila de la tabla "Vehículos adentro"
@TemplateData
public record InsideRowDto(Long recordId,
                           String plate,
                           String vehicleTypeLabel,
                           String vehicleIcon,
                           String ownerName,
                           String ownerTypeLabel,
                           String ownerTypeClass,   // role-student, role-teacher, ...
                           String zoneName,
                           String entryTime,
                           String stay) {           // permanencia, ej.: "2 h 15 min"
}
