package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Disponibilidad de una zona lista para pintar (tarjetas de la página principal
// y lista de zonas del formulario de entrada)
@TemplateData
public record ZoneCardDto(Long id,
                          String name,
                          String vehicleTypeCode,   // CAR o MOTORCYCLE
                          String vehicleTypeLabel,  // Carro o Moto
                          String vehicleIcon,       // clase del ícono (Bootstrap Icons)
                          int freeSpaces,
                          String freeLabel,         // "cupo libre" o "cupos libres"
                          int occupied,
                          int capacity,
                          int occupancyPercent,
                          String statusLabel,       // DISPONIBLE, POCOS CUPOS o LLENA
                          String statusClass) {     // available, few o full
}
