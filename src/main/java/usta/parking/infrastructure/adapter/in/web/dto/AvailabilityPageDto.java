package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

// Datos de la página principal (Disponibilidad)
@TemplateData
public record AvailabilityPageDto(List<ZoneCardDto> zones,
                                  int totalFree,
                                  int totalCapacity,
                                  int zoneCount,
                                  String updatedAt,          // hora en texto (HH:mm:ss)
                                  long updatedAtMillis) {    // para el contador "hace X segundos"
}
