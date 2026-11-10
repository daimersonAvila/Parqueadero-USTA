package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Una fila de la tabla "Zonas del parqueadero"
@TemplateData
public record ZoneRowDto(Long id,
                         String name,
                         String vehicleTypeLabel,
                         String vehicleIcon,
                         int capacity,
                         int occupied,           // vehículos adentro ahora
                         boolean active,
                         String statusLabel,     // "Activa" o "Cerrada"
                         boolean toggleTo,       // valor que se envía al mover el interruptor
                         String editHref) {      // enlace que abre el formulario en modo edición
}
