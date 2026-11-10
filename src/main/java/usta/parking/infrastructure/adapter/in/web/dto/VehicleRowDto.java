package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Una fila de la tabla "Vehículos registrados"
@TemplateData
public record VehicleRowDto(Long id,
                            String plate,
                            String vehicleTypeLabel,
                            String vehicleIcon,
                            String ownerName,
                            String ownerTypeLabel,
                            String ownerTypeClass,   // role-student, role-teacher, ...
                            String editHref) {       // enlace que abre el formulario en modo edición
}
